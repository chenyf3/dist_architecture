#!groovy
import com.xpay.dto.BuildDto

/**
 * 从 resources/build.env.json 文件中加载环境配置
 * @author chenyf
 * @param profile
 * @param appName
 * @param version
 * @return envDto
 */
def call(String profile, String appName, String version){
    BuildDto envDto = new BuildDto()

    def envConfig = libraryResource("build.env.json")//读取当前项目的 resources/build.env.json 文件，这是一个环境变量配置文件
    def configJsonObj = readJSON(text: envConfig) //要使用 readJSON() 方法需要安装 "Pipeline Utility Steps" 插件
    def envConfigObj = configJsonObj["${profile}"]
    def buildParam = envConfigObj['buildParam']
    envDto.setProfile(profile)//当前环境
    envDto.setAppName(appName)//应用名
    envDto.setVersion(version)//版本号
    envDto.setTeam((String) buildParam['team'])//业务团队名
    envDto.setRepoBase((String) buildParam['repoBase'])//代码仓库地址
    envDto.setRepoCredId((String) buildParam['repoCredId'])//代码仓库的访问凭证id(需要先添加到jenkins全局凭证中)
    envDto.setBranch((String) buildParam['branch'])//代码分支
    envDto.setDeployRootPath((String) buildParam['deployRootPath'])//项目部署根目录
    envDto.setImageRegAddr((String) buildParam['imageRegAddr'])//镜像仓库地址
    envDto.setImageRegCredId((String) buildParam['imageRegCredId'])//镜像仓库访问凭证(需要先添加到jenkins全局凭证中)

    String deployServerStr = (String) buildParam['deployServers']//部署当前应用的机器列表(需要先在jenkins中先配好)
    Map deployServerMap = readJSON(text: deployServerStr)//JSON层级在3层以上时需要转成 json string，然后重新解析Map（别问为什么，都是试出来的）
    if(appName != null && deployServerMap["${appName}"] != null){
        envDto.setDeployServers((String) deployServerMap["${appName}"])
    }else{
        envDto.setDeployServers((String) deployServerMap['defaultVal'])
    }

    Map javaOptsMap = envConfigObj['javaOpts']//java启动参数
    if(appName != null && javaOptsMap["${appName}"] != null){
        envDto.setJavaOpts((String) javaOptsMap["${appName}"])
    }else{
        envDto.setJavaOpts((String) javaOptsMap['defaultVal'])
    }
    return envDto
}