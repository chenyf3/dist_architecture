### 当前目录说明
**1.** 当前目录下的 jenkins-jobs 和 jenkins-libs 两个模块是在Jenkins中使用的，前者是Jenkins上每个任务的配置文件(JenkinsFile)，一个文件就代表一个Jenkins任务，后者是Jenkins的全局共享库(Global Pipeline Libraries)  
**2.** jenkins-libs 要作为全局共享库，需要在Jenkins中配置，共享库名称须为deploy-libs，因为在代码中写死了此名称，配置路径为 Manage Jenkins -> System Configuration -> Configure System 找到 Global Pipeline Libraries，然后配置共享库的名称、git地址 等等  
**3.** jenkins-jobs 和 jenkins-libs，都用到了Jenkins中的相关依赖工具：jdk8、maven3，需要先在Jenkins中配置好，配置路径为：Manage Jenkins -> System Configuration -> Global Tool Configuration 在这里配置JDK和MAVEN，其中JDK的别名为jdk8，maven的Name为maven3  
**4.** jenkins-jobs 和 jenkins-libs，都用到了Jenkins中的全局环境变量：PROFILE 代表当前环境，配置路径为：Manage Jenkins -> System Configuration -> Configure System 找到 "全局属性" 然后进行配置  
**5.** k8s-resources 是k8s部署业务应用的配置文件，这里的一个文件就代表这个业务应用的部署配置

### 其他
实际使用时建议把当前目录下的jenkins-jobs、jenkins-libs、k8s-resources模块放在一个单独git仓库地址，由开发和运维团队共同维护，再把业务代码单独一个库，由业务开发团队维护，这样方便代码管理和权限控制
