#!groovy
//1、引入jenkins定义好的全局共享库
library 'deploy-libs'// "Global Pipeline Libraries" 中定义的库名需为：deploy-libs

//2、构建参数并调用共享库中部署脚本
def map = [:]
map.put('appName', 'service-extend')
map.put('version', "${params.version}")
buildJob(map)