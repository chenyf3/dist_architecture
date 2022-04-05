# jenkins-libs 这是一个面向jenkins编程的项目  
### jenkins全局共享库(Global Pipeline Libraries)
**1**. 为了实现Docker镜像打包，需要在Jenkins部署的机器安装Docker  
**2**. 为了实现Docker镜像推送，如果私有镜像仓库使用自颁发CA证书，则需要在Jenkins部署的机器配置私有镜像仓库的CA证书，以实现https访问私有镜像仓库  
**3**. 为实现请求k8s集群进行项目部署，需要在Jenkins部署的机器安装kubectl命令  
**4**. 在Jenkins中配置全局共享库时需要命名为 deploy-libs，因为在jenkins-jobs模块下所有脚本引用全局共享库时在代码中写死的使用这个名字  
**5**. 需要在Jenkins中配置全局变量PROFILE，可选值为：dev、test、prod，因为在脚本中有引用此变量  
**6**. 需要在Jenkins中配置全局工具JAVA和Maven，并且分别命名为：jdk8、maven3，因为在脚本中用到此工具，并且在代码中写死了工具名是这两个  

### 在resources文件夹下有三个文件，各个文件的用处如下：  
**service.sh：** 此脚本用以通过ssh方式部署时，会随同jar包一起上传到目标机器，然后会执行此脚本来启动此jar程序  
**build.env.json：** 用以配置不同环境下项目构建时所需要的参数  
**publish.env.json：** 用以配置项目发布/部署时所需要的参数  

### build.env.json 文件配置参数说明如下：  
　team：               业务团队名称，会作为容器运行jar应用时的系统用户，同一个团队下的应用在镜像仓库中也会放到同一个项目下  
　repoBase：           代码库的访问地址  
　repoCredId：         访问代码库的jenkins凭据  
　branch：             代码分支  
　deployRootPath：     使用ssh部署时，目标机器的部署根目录  
　deployServers：      使用ssh部署时，部署到哪些机器，这些机器需要在jenkins中先配置好  
　　defaultVal：     默认部署的机器  
　　应用名：          配置具体的某个应用需要部署到哪些机器，如果没配置，则使用默认配置  
　imageRegAddr：       镜像仓库访问地址  
　imageRegCredId：     访问镜像仓库的jenkins凭据  
　javaOpts：           运行java应用的jvm参数，此参数值会设置到容器内的环境变量 JVM_OPTS，可在k8s的yaml文件中进行覆盖  
　　defaultVal：     默认jvm参数  
　　应用名：          配置具体的某个应用的jvm启动参数，如果没配置，则使用默认配置  
    
### publish.env.json 文件配置参数说明如下：  
　repoBase：           k8s的yaml文件代码仓库地址  
　repoCredId：         访问  
　branch：             代码分支  
　subPath：            k8s的yaml文件在代码仓库的子目录，如果有设置将会采用稀疏检出，如果没设置这会直接检出整个代码库的内容  
　idc：                机房相关配置，是一个对象数组  
　　code：           机房编码  
　　k8sUrl：         该机房的k8s集群访问地址  
　　k8sCredId：      该机房的k8s集群访问凭据  
　　regSecretName：  k8s从私有镜像仓库拉取镜像时使用的k8s Secret资源名称，意味着需要先在k8s中创建一个访问私有镜像仓库的Secret  
    






