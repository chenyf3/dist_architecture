#!groovy
import com.xpay.dto.PublishDto

/**
 * 从 resources/publish.env.json 文件中加载环境配置
 * @author chenyf
 * @param profile
 * @return envDto
 */
def call(String profile, String idc){
    PublishDto envDto = new PublishDto()

    String envConfig = libraryResource("publish.env.json")//读取当前项目的 resources/publish.env.json 文件，这是一个环境变量配置文件
    Map configJsonObj = readJSON(text: envConfig) //要使用 readJSON() 方法需要安装 "Pipeline Utility Steps" 插件
    Map envConfigObj = configJsonObj["${profile}"]
    envDto.setRepoBase((String) envConfigObj['repoBase'])//代码仓库地址
    envDto.setRepoCredId((String) envConfigObj['repoCredId'])//代码仓库的访问凭证id(需要先添加到jenkins全局凭证中)
    envDto.setBranch((String) envConfigObj['branch'])//代码分支
    envDto.setSubPath((String) envConfigObj['subPath'])//代码分支

    String idcStr = (String) envConfigObj['idc']
    List idcList = readJSON(text: idcStr)
    for(int i=0; i<idcList.size(); i++){
        Map idcCfg = idcList[i]
        if(idc.equals(idcCfg['code'])){
            envDto.setIdc(idc)//机房编号
            envDto.setK8sUrl((String) idcCfg['k8sUrl'])
            envDto.setK8sCredId((String) idcCfg['k8sCredId'])
            envDto.setK8sRegSecret((String) idcCfg['k8sRegSecret'])
            break
        }
    }
    return envDto
}