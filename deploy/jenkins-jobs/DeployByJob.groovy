#!groovy
/**
 * 当前脚本用以持续部署，从参数中传入需要部署的项目，然后会逐一触发这些项目所对应的jenkins任务来完成构建，当所有项目都构建完毕之后，
 * 再触发另一个jenkins任务，进行项目部署(ssh方式部署的，在构建完毕之后会马上进行部署)；全部项目都提交部署之后，会发送邮件通知和http回调
 * @author chenyf
 */
@Library('deploy-libs') // "Global Pipeline Libraries" 中定义的库名需为：deploy-libs
import com.xpay.utils.JobUtil
import com.xpay.utils.DeployUtil
import com.xpay.dto.BuildDto

profile = "${PROFILE}"//读取jenkins全局变量(需要先在jenkins中配置好)
startTime = 0L//任务开始时间
builtProjectList = []//已构建的项目名列表
deployedProjectList = []//已部署的项目名列表
totalAppNum = 0//需构建的项目总数
parallelNum = 3//并行执行数，不要超过jenkins中设置的"执行者数量"，否则会造成任务间相互等待而造成死锁状态，任务一直不会往前执行，卡住了
List serialJobList = []//需串行构建的任务列表
List parallelJobGroupList = [[]]//可并行构建的任务列表
List packageNameList = []//构建完成后产生的包名列表(jar包名或镜像名)
publishedIdc = ''//已发布机房

properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '25')),
            disableConcurrentBuilds()])

pipeline {
    //在任何可用的代理上执行Pipeline
    agent any

    options {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
    }

    //请求参数
    parameters {
        string(name: 'buildMsg', defaultValue: '', description: '构建说明信息')
        string(name: 'apps', defaultValue: '', description: '需要部署的项目')//多个项目用英文逗号分割
        string(name: 'idc', defaultValue: '', description: '部署机房')//构建完毕之后发布到哪个机房，多个机房则用英文的逗号分割
        string(name: 'version', defaultValue: '', description: '版本号')//构建镜像时将会使用此版本号
        string(name: 'relayApp', defaultValue: '', description: '中继项目')//排在这个项目之前的项目将不会再构建
        string(name: 'notifyEmail', defaultValue: '', description: '结果通知收件人')
        string(name: 'notifyUrl', defaultValue: '', description: '结果通知地址')
    }

    //声明使用到的工具(需要先在部署jenkins的机器安装好并在jenkins中配置好)
    tools {
        jdk 'jdk8'
        maven 'maven3'
    }

    stages {
        stage("入参打印") {
            steps {
                script {
                    deleteDir()//清空目录

                    startTime = new Date().getTime()

                    echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 构建参数 START"
                    echo "------- buildMsg：${params.buildMsg}"
                    echo "------- apps：${params.apps}"
                    echo "------- idc：${params.idc}"
                    echo "------- version：${params.version}"
                    echo "------- relayApp：${params.relayApp}"
                    echo "------- notifyEmail：${params.notifyEmail}"
                    echo "------- notifyUrl：${params.notifyUrl}"
                    echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 构建参数 END"
                }
            }
        }

        stage("job名转换") {
            steps {
                script {
                    //把应用名称转换成jenkins中任务名
                    String[] appArr = params.apps.split(",")
                    int doneIdx = JobUtil.getIndex(appArr, params.relayApp)
                    def currGroupList = []
                    totalAppNum = appArr.length

                    for(int i=0; i<totalAppNum; i++){
                        String appName = appArr[i] != null ? appArr[i].trim() : null
                        if(appName == null || appName.trim().length() == 0){
                            continue
                        }
                        String jobName = JobUtil.getJobName(appName)

                        //此场景是这样的，比如第一次提交了10个项目，构建到中间某个(如第6个)应用时失败了，人工解决后重新发起部署请求，此时可以
                        //传入参数 relayApp 为第6个应用名，那么就会跳过前面5个应用的构建，从第6个应用开始构建，需要注意的是第1次到第N次请求
                        //apps、version 这两个参数的值要不变
                        if(i < doneIdx){
                            if(DeployUtil.isPackageImage(profile, appName)){
                                String version = params.version
                                BuildDto envDto = loadBuildEnv(profile, appName, version)//调用 loadEnv.groovy 脚本中的 loadBuildEnv() 方法加载环境配置
                                String imageName = DeployUtil.getImageName(envDto.getImageRegAddr(), envDto.getTeam(), appName, version)
                                packageNameList.add(imageName)
                                echo "--------------------------------- ${jobName} 已执行过，直接装填镜像: ${imageName}"
                            }else{
                                echo "--------------------------------- ${jobName} 已执行过，忽略"
                            }
                            builtProjectList.add(appName)
                            continue
                        }

                        boolean isDeploy = DeployUtil.isNeedDeploy(appName)
                        if(isDeploy){
                            currGroupList.add(jobName)

                            if(currGroupList.size() == parallelNum){ //达到换组条件
                                parallelJobGroupList.add(currGroupList)
                                currGroupList = []//换成新数组
                            }
                        }else{
                            serialJobList.add(jobName)
                        }
                    }

                    if(currGroupList.size() > 0 && !parallelJobGroupList.contains(currGroupList)){//避免最后一个组没有被添加进来
                        parallelJobGroupList.add(currGroupList)
                    }
                }
            }
        }

        stage("串行任务构建") {
            steps {
                script {
                    if(serialJobList == null || serialJobList.size() == 0){
                        echo '--------------------------------- 没有需要串行构建的任务，本阶段将跳过'
                        return
                    }

                    //先构建需要串行执行的任务，一般来说都是被其他项目依赖且无需部署的项目
                    for (int i=0; i<serialJobList.size(); i++) {
                        String jobName = serialJobList.get(i)
                        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ${jobName} 开始执行"
                        //将会返回 org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper 类的实例对象
                        def runWrapper = build(job: "${jobName}", quietPeriod: 0, parameters: [
                                string(name: 'version', value: "${params.version}")]
                        )
                        int duration = (int) (runWrapper.getDuration()/1000)//持续时间(秒)
                        if("success".equalsIgnoreCase(runWrapper.getResult())){
                            builtProjectList.add(jobName)
                        }
                        echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ${jobName} 执行完毕(${runWrapper.getResult()})(${duration}秒)"
                    }
                }
            }
        }

        stage("并行任务构建") {
            steps {
                script {
                    if(parallelJobGroupList == null || parallelJobGroupList.size() == 0){
                        echo '--------------------------------- 没有需要并行构建的任务，本阶段将跳过'
                        return
                    }

                    //并行执行任务
                    for (int i=0; i<parallelJobGroupList.size(); i++) {
                        List currGroupJob = parallelJobGroupList.get(i)
                        if(currGroupJob.size() == 0){
                            continue
                        }

                        def parallelJobMap = [:]
                        for(int j=0; j<currGroupJob.size(); j++){
                            def jobName = currGroupJob.get(j)
                            parallelJobMap.put(jobName, {
                                echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ${jobName} 开始执行"
                                //将会返回 org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper 类的实例对象
                                def runWrapper = build(job: "${jobName}", quietPeriod: 0, parameters: [
                                        string(name: 'version', value: "${params.version}")]
                                )
                                int duration = (int) (runWrapper.getDuration()/1000)//持续时间(秒)
                                echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ${jobName} 执行完毕(${runWrapper.getResult()})(${duration}秒)"

                                if("success".equalsIgnoreCase(runWrapper.getResult())){
                                    builtProjectList.add(jobName)

                                    Map variablesMap = runWrapper.getBuildVariables()
                                    String packageName = variablesMap.get("PACKAGE_NAME")
                                    if('deployed'.equals(packageName)){
                                        deployedProjectList.add(jobName)
                                    }else if(packageName != null && packageName.trim().length() > 0){
                                        packageNameList.add(packageName)
                                    }
                                }
                            })
                        }
                        parallel parallelJobMap //并行执行任务
                    }
                }
            }
        }

        stage("项目部署") {
            steps {
                script {
                    String publishJob = DeployUtil.getPublishJob()

                    //发布到机房
                    if(params.idc == null || params.idc.length() == 0){
                        echo "--------------------------------- 发布更新的机房为空，不执行发布更新"
                        return
                    }else if(publishJob == null || publishJob.length() == 0){
                        echo "--------------------------------- 没有配置项目发布任务，不执行发布更新"
                        return
                    }else if(packageNameList.size() <= 0){
                        echo "--------------------------------- 没有需要部署的项目，不执行发布更新"
                        return
                    }

                    //触发发布更新的任务，把项目部署到k8s集群上
                    String packageNameStr = packageNameList.join(',') //转换成以逗号分割的字符串
                    String[] idcArr = params.idc.split(',')
                    for(int i=0; i<idcArr.length; i++){
                        String idc = idcArr[i]

                        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 开始发布到机房: ${idc}"
                        def runWrapper = build(job: "${publishJob}", quietPeriod: 0, parameters: [
                                string(name: 'publishMsg', value: "${params.buildMsg}"),
                                string(name: 'idc', value: "${idc}"),
                                string(name: 'packageNames', value: "${packageNameStr}")]
                        )
                        if("success".equalsIgnoreCase(runWrapper.getResult())){
                            publishedIdc += "${idc}"
                        }
                        echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 发布更新完毕(${runWrapper.getResult()})"
                    }
                }
            }
        }
    }

    //根据pipeline运行的不同结果来发送邮件通知
    post {
        success {
            notifyResult(status.SUCCESS, params.notifyUrl, params.notifyEmail)
            cleanWs()
        }
        failure {
            notifyResult(status.FAILURE, params.notifyUrl, params.notifyEmail)
            deleteDir()
        }
        unstable {
            notifyResult(status.UNSTABLE, params.notifyUrl, params.notifyEmail)
            cleanWs()
        }
        aborted {
            notifyResult(status.ABORTED, params.notifyUrl, params.notifyEmail)
            cleanWs()
        }
    }
}

/**
 * 结果通知
 * @param result
 * @param url
 * @return
 */
def notifyResult(String result, String notifyUrl, String notifyEmail){
    //发送http回调通知
    try {
        sendHttpResult(notifyUrl, result)
    } catch(Exception e) {
        echo "发送回调通知时出现异常：${e.getMessage()}"
    }

    try {
        sendEmailResult(notifyEmail, result)
    } catch(Exception e) {
        echo "发送邮件通知时出现异常：${e.getMessage()}"
    }
}

/**
 * 发送http回调
 * @param notifyUrl
 * @param result
 * @return
 */
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

/**
 * 发送邮件通知
 * @param notifyEmail
 * @param result
 * @return
 */
def sendEmailResult(String notifyEmail, String result){
    if (notifyEmail == null || notifyEmail.trim().length() <= 0) {
        return
    }

    String resultMsg = ''
    if(status.SUCCESS.equals(result)){
        resultMsg = '成功'
    }else if(status.FAILURE.equals(result)){
        resultMsg = '失败'
    }else if(status.UNSTABLE.equals(result)){
        resultMsg = 'unstable'
    }else if(status.ABORTED.equals(result)){
        resultMsg = '取消'
    }

    int builtAppNum = builtProjectList.size()
    int costSecond = (int) (new Date().getTime() - startTime)/1000L
    int minute = (int) Math.floor(costSecond/60d)
    int second = costSecond - minute * 60
    String costTime = "${minute}分${second}秒"

    String to = JobUtil.getEmailTo(notifyEmail)
    String cc = JobUtil.getEmailCC(notifyEmail)
    String subject = "部署${resultMsg}: ${params.buildMsg}"
    String body = "【构建${resultMsg}】 构建地址：${env.BUILD_URL}，构建环境：${profile}"
    if(params.idc != null && params.idc.trim().length() > 0){
        body += "，目标机房：${params.idc}，已发布机房：${publishedIdc}"
    }
    body += "，耗时：${costTime}，所有项目为(${totalAppNum}个)：${params.apps}"
    if(totalAppNum == builtAppNum){
        body += '，全部都构建成功'
    }else{
        body += "，已成功构建项目为(${builtAppNum}个)：" + builtProjectList.join(',')
    }

    //使用内置邮件插件进行发送，需要先在jenkins中配置好邮件服务器
    echo "--------------------------------- 发送邮件通知"
    mail (to: "${to}", cc: "${cc}", subject: "${subject}", body: "${body}")
}
