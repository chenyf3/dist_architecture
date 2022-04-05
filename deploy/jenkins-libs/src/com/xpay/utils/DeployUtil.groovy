package com.xpay.utils

/**
 * 项目部署辅助工具类
 */
public class DeployUtil {

    public static String getPublishJob(){
        return 'deploy-publish'
    }

    public static boolean isDev(String profile){
        return 'dev'.equalsIgnoreCase(profile)
    }

    public static boolean isTest(String profile){
        return 'test'.equalsIgnoreCase(profile)
    }

    public static boolean isProd(String profile){
        return 'prod'.equalsIgnoreCase(profile)
    }

    /**
     * 根据应用名获取该应用的打包名称
     * @param appName
     * @return
     */
    public static String getPackageName(String appName){
        return appName
    }

    /**
     * 根据应用名取得该应用的类型，会使用此名称进行代码库地址路径补齐等处理
     * @param appName
     * @return
     */
    public static String getProjectType(String appName){
        if(appName.indexOf("common") == 0){
            return "common"
        }else if(appName.indexOf("lib") == 0){
            return "libs"
        }else if(appName.indexOf("starter") == 0){
            return "starter"
        }else if(appName.indexOf("facade") == 0){
            return "facade"
        }else if(appName.indexOf("service") == 0){
            return "service"
        }else if(appName.indexOf("web") == 0){
            return "web"
        }else if(appName.indexOf("gateway") == 0){
            return "gateway"
        }else if(appName.indexOf("ware") == 0){
            return "middleware"
        }else{
            throw new IllegalArgumentException("无法为当前应用匹配项目类型，appName：$appName")
        }
    }

    /**
     * 判断该应用是否需要发布部署
     * @param appName
     * @return
     */
    public static boolean isNeedDeploy(String appName){
        String projectType = getProjectType(appName)
        if("libs".equalsIgnoreCase(projectType) || "common".equalsIgnoreCase(projectType)
                || "starter".equalsIgnoreCase(projectType) || "facade".equalsIgnoreCase(projectType)){
            return false
        }else if("web-api-common".equalsIgnoreCase(appName)){
            return false
        }else{
            return true
        }
    }

    /**
     * 根据应用名判断是否属于纯静态项目
     * @param appName
     * @return
     */
    public static boolean isStaticProject(String appName){
        return appName != null && appName.endsWith("view")
    }

    /**
     * 是否需要打包成镜像
     * @param profile
     * @param appName
     * @return
     */
    public static boolean isPackageImage(String profile, String appName){
        return isNeedDeploy(appName) && isProd(profile)
    }

    /**
     *
     * @return
     */
    public static String getCurrSecond(){
        return new Date().format("yyMMddHHmmss")
    }

    /**
     * 构建镜像名称
     */
    public static String getImageName(String imageRegAddr, String team, String appName, String version){
        if(version == null || version.trim().length() == 0){
            version = 'latest'
        }
        return "${imageRegAddr}/${team}/${appName}:${version}"
    }
}
