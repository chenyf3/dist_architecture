#!groovy
/**
 * 部署发布，真正执行发布项目的任务
 * @author chenyf
 */
@Library('deploy-libs') // "Global Pipeline Libraries" 中定义的库名需为：deploy-libs
import com.xpay.dto.BuildDto
import com.xpay.dto.PublishDto
import com.xpay.utils.DeployUtil

profile = "${PROFILE}"//读取jenkins全局变量(需要先在jenkins中配置好)
BuildDto buildDto = null //项目构建变量对象
PublishDto pubEnvDto = null //项目发布变量对象
appMapImageName = [:]

properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '25')),
            disableConcurrentBuilds()])

pipeline {
    agent any

    options {
        timeout(time: 5, unit: 'MINUTES')
        timestamps()
    }

    //请求参数
    parameters {
        string(name: 'publishMsg', defaultValue: '', description: '项目发布备注')
        choice(name: 'idc', description: '发布机房', choices: ['--请选择--', 'sz', 'gz'])//请修改成自己真实的机房编号
        string(name: 'packageNames', defaultValue: '', description: '项目包列表')
        string(name: 'notifyUrl', defaultValue: '', description: '回调通知地址')
    }

    stages {
        stage("入参打印") {
            steps {
                script {
                    echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 发布参数 START"
                    echo "------- publishMsg：${params.publishMsg}"
                    echo "------- idc：${params.idc}"
                    echo "------- packageNames：${params.packageNames}"
                    echo "------- notifyUrl：${params.notifyUrl}"
                    echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 发布参数 END"
                }
            }
        }

        stage('参数初始化') {
            steps {
                script {
                    String idcCode = params.idc
                    pubEnvDto = loadPublishEnv(profile, idcCode)//调用 loadEnv.groovy 脚本中的 loadPublishEnv() 方法加载环境配置
                    if(pubEnvDto.getIdc() == null || pubEnvDto.getIdc().trim().length() == 0){
                        error("未预期的发布机房：${idcCode}！")//抛出错误，令流程中止
                    }

                    String packageNames = params.packageNames
                    if(packageNames == null || packageNames.trim().length() == 0){
                        error("请指定要发布的项目镜像！")//抛出错误，令流程中止
                    }

                    buildDto = loadBuildEnv(profile, "", "")
                    String imageRegAddr = buildDto.getImageRegAddr()
                    String team = buildDto.getTeam()
                    String[] packageNameArr = params.packageNames.split(',')
                    for(int i=0; i<packageNameArr.length; i++){
                        String packageName = packageNameArr[i]
                        if(packageName == null || packageName.trim().length() == 0){
                            continue
                        }

                        int startIndex = packageName.lastIndexOf('/') + 1
                        int endIndex = packageName.lastIndexOf(':')
                        String appName = packageName.substring(startIndex, endIndex)

                        if(! DeployUtil.isNeedDeploy(appName)){
                            continue
                        }else if(packageName.startsWith(imageRegAddr)){ //包名已是完整的镜像名，形如：registry.xpay.com/payment/service-user:2108100000001
                            appMapImageName.put(appName, packageName)
                            continue
                        }else{ //包名还不是完整的镜像名，形如：service-user:2108100000001
                            String version = packageName.substring(endIndex + 1)
                            String imageName = DeployUtil.getImageName(imageRegAddr, team, appName, version)
                            appMapImageName.put(appName, imageName)
                        }
                    }
                }
            }
        }

        stage('拉取部署yaml文件') {
            steps {
                script {
                    deleteDir()//检出代码前先清空当前目录，避免有旧文件导致检出失败或者没有更新

                    String subPath = pubEnvDto.getSubPath()
                    if (subPath != null && subPath.length() > 0) {
                        String repoPath = "/${subPath}"//前面加一个/ 表示从项目根路径查找
                        // 从git代码库checkout代码(采用稀疏检出的方式)
                        checkout([$class: 'GitSCM',
                                  branches: [[name: "*/${pubEnvDto.getBranch()}"]],
                                  doGenerateSubmoduleConfigurations: false,
                                  extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: "${repoPath}"]]]],
                                  submoduleCfg: [],
                                  userRemoteConfigs: [[credentialsId: "${pubEnvDto.getRepoCredId()}", url: "${pubEnvDto.getRepoBase()}"]]
                        ])

                        sh "mv ${subPath}/*.yaml ./"
                    } else {
                        checkout([$class: 'GitSCM',
                                  branches: [[name: "*/${pubEnvDto.getBranch()}"]],
                                  doGenerateSubmoduleConfigurations: false,
                                  submoduleCfg: [],
                                  userRemoteConfigs: [[credentialsId: "${pubEnvDto.getRepoCredId()}", url: "${pubEnvDto.getRepoBase()}"]]
                        ])
                    }
                }
            }
        }

        stage('发布更新') {
            steps {
                script {
                    if(appMapImageName.size() <= 0){
                        echo "--------------------------------- 没有需要发布的项目，本步骤将跳过！"
                        return
                    }

                    String k8sRegSecret = pubEnvDto.getK8sRegSecret()
                    //替换k8s中连接镜像仓库拉取镜像的密钥名占位符
                    sh "sed -i \"s#<REG_SECRET_NAME>#${k8sRegSecret}#g\" *.yaml"

                    List yamlFileNames = []
                    appMapImageName.each { key,val ->
                        String appName = key
                        String imageName = val
                        //把镜像名称占位符替换成真实的值
                        String yamlName = "${appName}.yaml"
                        sh "sed -i \"s#<IMAGE_NAME>#${imageName}#g\" ${yamlName}"
                        yamlFileNames.add(yamlName)
                    }

                    String k8sUrl = pubEnvDto.getK8sUrl()
                    String k8sCredId = pubEnvDto.getK8sCredId()
                    echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 发布更新到 ${params.idc} 机房：${params.publishMsg}"
                    for (int i=0; i<yamlFileNames.size(); i++) {
                        String yamlFile = yamlFileNames[i]
                        //需要安装 "Kubernetes CLI" 插件
                        withKubeConfig([serverUrl: "${k8sUrl}", credentialsId: "${k8sCredId}"]) {
                            sh "/usr/local/bin/kubectl apply -f ${yamlFile}" //此处注意kubectl的路径要写对
                        }
                    }
                    echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 发布更新完毕${params.idc}"
                }
            }
        }
    }

    post {
        success {
            sendHttpResult(params.notifyUrl, status.SUCCESS)
            cleanWs()
        }
        failure {
            sendHttpResult(params.notifyUrl, status.FAILURE)
            deleteDir()
        }
        unstable {
            sendHttpResult(params.notifyUrl, status.UNSTABLE)
            cleanWs()
        }
        aborted {
            sendHttpResult(params.notifyUrl, status.ABORTED)
            cleanWs()
        }
    }
}

def sendHttpResult(String notifyUrl, String result){
    if (notifyUrl == null || notifyUrl.trim().length() <= 0) {
        return
    }

    if(!notifyUrl.startsWith("http://") && !notifyUrl.startsWith("https://")){
        notifyUrl = "http://" + notifyUrl
    }

    if(notifyUrl.lastIndexOf("?") <= -1){
        notifyUrl = notifyUrl + "?result=${result}"
    }else{
        notifyUrl = notifyUrl + "&result=${result}"
    }

    boolean isRetry = true
    int tryTimes = 0
    while(isRetry){
        try{
            tryTimes ++
            //要调用 httpRequest() 方法需要安装 "HTTP Request" 插件
            echo "--------------------------------- 发送回调通知第 ${tryTimes} 次"
            def response = httpRequest(url:notifyUrl, httpMode:'GET', ignoreSslErrors:true, timeout:6)
            if(response.status >= 200 && response.status < 300){
                isRetry = false
            }
        }catch(Exception e){
            echo "--------------------------------- 发送回调通知时出现异常：errMsg: ${e.getMessage()}"
        }
        if(isRetry && tryTimes >= 3){
            isRetry = false
        }
    }
}

