#!groovy
import com.xpay.utils.DeployUtil
import com.xpay.dto.BuildDto

/**
 * 部署项目
 * @author chenyf
 *
 * @param map
 * map中参数有：
 *  appName:    应用名称，必填
 *  version:    版本号，选填
 *
 * @return
 */
def call(Map map) {
    //配置不允许并发构建，构建个数保留10个，发布包保留10个，配置一个version参数
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')),
                parameters([string(name: 'version', defaultValue: '', description: '版本号', trim: true)]),
                disableConcurrentBuilds()])

    pipeline {
        //在任何可用的代理上执行Pipeline
        agent any

        options {
            timeout(time: 10, unit: 'MINUTES')
            timestamps()
        }

        //环境变量，需要在jenkins中预先配置好名称和路径，确定之后一般不需更改
        tools {
            jdk 'jdk8'
            maven 'maven3'
        }

        //pipeline的各个阶段场景
        stages {
            stage('项目构建') {
                steps {
                    script {
                        deleteDir()//先清空当前目录，避免遗留有旧文件

                        String profile = "${PROFILE}"//从jenkins全局变量中取得，需要在jenkins中先配置好
                        String appName = "${map.appName}"
                        String version = "${map.version}"
                        if(version == null || "null".equalsIgnoreCase(version) || version.length() == 0){
                            version = DeployUtil.getCurrSecond()
                        }

                        echo "开始构建项目 ---------------> appName: ${appName}, profile: ${profile}"
                        BuildDto envDto = loadBuildEnv(profile, appName, version)// 调用 loadPublishEnv.groovy 脚本加载环境配置
                        def returnValue = pack(envDto)// 调用 pack.groovy 脚本执行部署
                        if(returnValue != null){
                            env.PACKAGE_NAME = returnValue//设置一个变量，调用构建任务的地方可通过 RunWrapper#getBuildVariables() 获取到此变量值
                        }
                    }
                }
            }
        }

        post {
            success {
                cleanWs()
            }
            failure {
                deleteDir()
            }
            unstable {
                cleanWs()
            }
            aborted {
                cleanWs()
            }
        }
    }
}


