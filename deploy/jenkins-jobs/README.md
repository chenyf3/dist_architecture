# jenkins-jobs 这是一个面向jenkins编程的项目  
### jenkins任务文件，这里的一个文件就代表在jenkins中的一个任务  

### jenkins在默认安装完毕之后，还需要额外安装如下插件：  
**1.** HTTP Request 插件，在发送部署结果通知时会用到此插件  
**2.** Docker Pipeline 插件，在进行docker镜像推送时会用到此插件  
**3.** Pipeline Utility Steps 插件，在调用 readJSON() 方法来解析json配置文件时会用到此插件  
**4.** Kubernetes CLI 插件，在使用kubectl命令请求k8s集群进行部署时会用到此插件  

### 其他说明：
**1.** 需要在Jenkins中配置全局变量PROFILE，可选值为：dev、test、prod，因为在脚本中有引用此变量  
**2.** 需要在Jenkins中配置全局工具JAVA和Maven，并且分别命名为：jdk8、maven3，因为在脚本中用到此工具，并且在代码中写死了工具名是这两个  
