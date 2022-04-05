#!groovy
import com.xpay.dto.BuildDto
import com.xpay.dto.SSHDto
import com.xpay.utils.DeployUtil

//全局变量
appName = ""//应用名称
profile = ""//当前部署环境
isStaticProject = false//是否前端静态项目

/**
 * 应用打包脚本，将根据部署环境需要打包成：jar包、tar包、docker镜像等
 * @author chenyf
 * @param envDto
 * @return String 有3种返回值：
 *  null：       表示当前项目已构建完毕且不需要部署
 *  deployed：   表示当前项目已部署完成
 *  具体的镜像名： 表示当前项目被打包成了镜像
 */
def call(BuildDto envDto) {
    stage('代码检出') {
        script {
            deleteDir()//检出代码前先清空当前目录，避免有旧文件导致检出失败或者没有更新

            appName = envDto.getAppName()
            profile = envDto.getProfile()
            isStaticProject = DeployUtil.isStaticProject(appName)
            def projectType = DeployUtil.getProjectType(appName)
            def repoUrl = "${envDto.getRepoBase()}"

            if (repoUrl.endsWith(".git")) {
                def subDirect = "${projectType}/${appName}"
                def repoPath = "/${subDirect}"//前面加一个/ 表示从项目根路径查找
                // 从git代码库checkout代码(采用稀疏检出的方式)
                checkout([$class: 'GitSCM',
                          branches: [[name: "*/${envDto.getBranch()}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: "${repoPath}"]]]],
                          submoduleCfg: [],
                          userRemoteConfigs: [[credentialsId: "${envDto.getRepoCredId()}", url: "${repoUrl}"]]
                ])

                if(isStaticProject){
                    //如果是前端项目，则需要要包含隐藏文件，因为前端项目有类似 .eslintrc.js .editorconfig 这种文件
                    sh "mv ${subDirect}/* ${subDirect}/.[^.]* ./;"
                }else{
                    sh "mv ${subDirect}/* ./;"
                }
            } else {
                // 从svn代码库checkout代码
                def repoPath = "${repoUrl}/${envDto.getBranch()}/${projectType}/${appName}"
                checkout([$class: 'SubversionSCM',
                          additionalCredentials: [],
                          excludedCommitMessages: '',
                          excludedRegions: '',
                          excludedRevprop: '',
                          excludedUsers: '',
                          filterChangelog: false,
                          ignoreDirPropChanges: false,
                          includedRegions: '',
                          locations: [[cancelProcessOnExternalsFail: true,
                                       credentialsId: "${envDto.getRepoCredId()}",
                                       depthOption: 'infinity',
                                       ignoreExternalsOption: true,
                                       local: '.',
                                       remote: "${repoPath}"]],
                          quietOperation: true,
                          workspaceUpdater: [$class: 'UpdateUpdater']
                ])
            }
        }
    }

    stage('应用打包 & 本地安装') {
        sh "mvn clean install -P${profile} -Dmaven.test.skip=true"

        script {
            if(isStaticProject){
                String packageName = DeployUtil.getPackageName(appName)
                sh "tar -cf ${packageName}.tar dist/*"
            }
        }
    }

    stage('打包镜像 | SSH部署') {
        script {
            if(! DeployUtil.isNeedDeploy(appName)){
                echo "${appName} 本地仓安装已完成，不需要部署"
                return null
            }

            //生产环境使用k8s部署，需要打包成Docker镜像上传到私有镜像仓库，开发&测试环境直接在物理机上部署，需要把打包好的文件上传到指定服务器并重启
            boolean isPackImage = DeployUtil.isPackageImage(profile, appName)//是否需要打包成镜像
            String javaOpts = envDto.getJavaOpts() == null ? "" : envDto.getJavaOpts()

            if (isPackImage) {
                String team = envDto.getTeam()
                String version = envDto.getVersion()
                String imageRegAddr = envDto.getImageRegAddr()
                String packageName = DeployUtil.getPackageName(appName)
                String imageName = DeployUtil.getImageName(imageRegAddr, team, appName, version)
                String dockerFileName = "Dockerfile"
                if (isStaticProject) {
                    String unTarFolder = "${appName}"//解压目录
                    sh "mkdir ${unTarFolder} && tar -xf ${packageName}.tar -C ${unTarFolder};"//文件解压
                    String dockerfile = genTarDockerfile(imageRegAddr, team, unTarFolder)
                    echo "Dockerfile -------------->\n${dockerfile}\n<-------------- Dockerfile"
                    writeFile(file: "${dockerFileName}", text: dockerfile)//创建Dockerfile并写入内容
                    sh "docker build -f ${dockerFileName} -t ${imageName} ."
                } else {
                    String workdir = "/home/${team}"
                    String entrypointName = "entrypoint.sh"
                    String jarName = "${packageName}.jar"
                    String jarFullName = "${workdir}/${jarName}"
                    String entrypointSh = genEntrypointShell(jarFullName)
                    echo "${entrypointName} -------------->\n${entrypointSh}\n<-------------- ${entrypointName}"
                    writeFile(file: entrypointName, text: entrypointSh)//创建entrypoint.sh并写入内容
                    sh "chmod u+x ${entrypointName}"

                    String dockerfile = genJarDockerfile(imageRegAddr, team, workdir, jarName, javaOpts, entrypointName)
                    echo "Dockerfile -------------->\n${dockerfile}\n<-------------- Dockerfile"
                    writeFile(file: "${dockerFileName}", text: dockerfile)//创建Dockerfile并写入内容
                    sh "docker build -f ${dockerFileName} -t ${imageName} ."
                }

                //推送镜像到镜像仓库(需要安装 "Docker Pipeline" 插件)
                withDockerRegistry([url: "https://${imageRegAddr}", credentialsId: "${envDto.getImageRegCredId()}"]) {
                    sh "docker push ${imageName}"
                    sh "docker rmi -f ${imageName} && docker logout ${imageRegAddr}"//清除本地的镜像，避免占用磁盘空间，然后退出镜像仓库的登录
                }
                echo "------> 镜像推送完成：${imageName} <------"
                return imageName
            } else {
                if(! isStaticProject){
                    String shellContent = libraryResource("service.sh")
                    writeFile(file: "target/service.sh", text: shellContent)
                    //执行这一步是为了解决 service.sh 文件在 Windows 系统下编辑过而在unix上运行异常的情况（/bin/bash^M: 坏的解释器: 没有那个文件或目录）
                    sh "sed -i 's/\\r\$//' target/service.sh;"
                    //替换占位符中的内容，包括：JVM启动参数、项目名称
                    sh "sed -i \"s#@JAVA_OPTS@#${javaOpts}#g\" target/service.sh"
                    sh "sed -i \"s#@APP_NAME@#${appName}#g\" target/service.sh"
                }

                SSHDto sshDto = getSSHDeployDto(profile, appName, envDto.getDeployRootPath())
                def servers = envDto.getDeployServers().split(",")
                for(int i=0; i<servers.length; i++){
                    def serverName = servers[i]
                    echo "开始部署：${appName}  ----->  ${serverName}"
                    sshPublisher(publishers: [
                            sshPublisherDesc(configName: "${serverName}",
                                    transfers: [
                                            sshTransfer(cleanRemote: false,
                                                    excludes: '',
                                                    sourceFiles: "${sshDto.getSourceFiles()}",
                                                    removePrefix: "${sshDto.getRemovePrefix()}",
                                                    remoteDirectory: "${sshDto.getRemoteDirectory()}",
                                                    execCommand: "${sshDto.getDeployCommand()}",
                                                    execTimeout: 120000,
                                                    flatten: false,
                                                    makeEmptyDirs: false,
                                                    noDefaultExcludes: false,
                                                    patternSeparator: '[, ]+',
                                                    remoteDirectorySDF: false
                                            )
                                    ],
                                    usePromotionTimestamp: false,
                                    useWorkspaceInPromotion: false,
                                    verbose: false
                            )
                    ])
                }
                return "deployed"
            }
        }
    }
}

/**
 * 为jar包生成构建docker镜像的Dockerfile文件内容
 * @param imageRegAddr      镜像仓库地址
 * @param team              团队名称
 * @param workdir           容器的工作目录
 * @param jarName           jar包文件名
 * @param entrypointName    容器启动时的入口文件名
 * @return
 */
def genJarDockerfile(String imageRegAddr, String team, String workdir, String jarName, String jvmOpts, String entrypointName) {
    jvmOpts = jvmOpts == null ? "" : jvmOpts

    return "FROM ${imageRegAddr}/base/jdk:8\n" +
            "LABEL version=\"1.0\" \\\n" +
            "      team=\"${team}\" \\\n" +
            "      env=\"prod\"\n" +
            "MAINTAINER <image_builder@xpay.com>\n" +
            "RUN useradd -m ${team} -u 1001 \\\n" +
            "    && mkdir ${workdir}/logs\n" +
            "WORKDIR ${workdir}\n" +
            "COPY target/${jarName} ${workdir}\n" +
            "COPY ${entrypointName} /usr/local/bin/${entrypointName}\n" +
            "ENV JVM_OPTS=\"${jvmOpts}\"\n" + //设置 JVM_OPTS 系统变量，以供后续的调优(可在k8s中覆盖)
            "ENTRYPOINT [\"${entrypointName}\"]\n" +
            "CMD [\"execute\"]\n" +
            "RUN chown -R ${team}.${team} ${workdir} \n" +
            "USER ${team}"
}

/**
 * 生成容器入口文件shell脚本
 * @param javaOpts
 * @param jarFullName
 * @return
 */
def genEntrypointShell(String jarFullName) {
    return "#!/bin/sh\n" +
            "source /etc/profile\n" +
            "source ~/.bash_profile\n" +
            "case \"\$1\" in\n" +
            "  *)\n" +
            "     exec java \${JVM_OPTS} -jar ${jarFullName}\n" + //JVM_OPTS 这个系统环境变量需要在Dockerfile中设置
            "esac\n" +
            "exit 0"
}

/**
 * 为tar包(前端项目)生成构建docker镜像的Dockerfile文件内容
 * @param imageRegAddr      镜像仓库地址
 * @param team              团队名称
 * @param unTarFolder       tar包的解压目录
 * @return
 */
def genTarDockerfile(String imageRegAddr, String team, String unTarFolder) {
    return "FROM ${imageRegAddr}/base/nginx:1.21.1\n" +
            "LABEL version=\"1.0\" \\\n" +
            "      team=\"${team}\" \\\n" +
            "      env=\"prod\"\n" +
            "MAINTAINER <image_builder@xpay.com>\n" +
            "RUN rm -rf /usr/local/nginx/html/*\n" +
            "COPY ${unTarFolder}/* /usr/local/nginx/html/\n" +
            "RUN chown -R nginx.nginx /usr/local/nginx/html"
}

/**
 * 取得SSH方式部署时的各种参数和部署命令
 * @param appName           应用名
 * @param deployRootPath    应用部署根目录
 * @return sshDto
 */
def getSSHDeployDto(String appName, String deployRootPath) {
    String sourceFiles = "", removePrefix = "", remoteDirectory = "", deployCommand = ""
    String packageName = DeployUtil.getPackageName(appName)
    if (DeployUtil.isStaticProject(appName)) {
        sourceFiles = "${packageName}.tar"
        remoteDirectory = "view"
        String deployFullPath = "${deployRootPath}/${remoteDirectory}/"
        deployCommand = "cd ${deployFullPath};" +
                "rm -r ${packageName};" +
                "mkdir ${packageName};" +
                "tar -xf ${packageName}.tar -C ${packageName};" +
                "rm ${packageName}.tar"
    } else {
        String projectType = DeployUtil.getProjectType(appName)
        sourceFiles = "target/${packageName}.jar,target/service.sh"
        removePrefix = "target"
        remoteDirectory = "${projectType}/${packageName}"
        String deployFullPath = "${deployRootPath}/${remoteDirectory}/"
        deployCommand = "cd ${deployFullPath}" +
                "chmod u+x ./service.sh;" +
                "./service.sh restartF"
    }

    SSHDto sshDto = new SSHDto()
    sshDto.setSourceFiles(sourceFiles)
    sshDto.setRemovePrefix(removePrefix)
    sshDto.setRemoteDirectory(remoteDirectory)
    sshDto.setDeployCommand(deployCommand)
    return sshDto
}