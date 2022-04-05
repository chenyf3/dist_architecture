package com.xpay.dto

/**
 * 任务构建参数
 */
class BuildDto {
    /**
     * 当前部署环境：dev/test/prod
     */
    private String profile
    /**
     * 应用名称
     */
    private String appName
    /**
     * 业务团队
     */
    private String team
    /**
     * 代码仓库地址
     */
    private String repoBase
    /**
     * 代码分支
     */
    private String branch
    /**
     * 代码仓库拉取代码的凭证id
     */
    private String repoCredId
    /**
     * 部署根目录
     */
    private String deployRootPath
    /**
     * 部署服务器
     */
    private String deployServers
    /**
     * java启动参数
     */
    private String javaOpts
    /**
     * 镜像仓库地址
     */
    private String imageRegAddr
    /**
     * 镜像仓库访问凭证id
     */
    private String imageRegCredId
    /**
     * 镜像版本号
     */
    private String version

    String getProfile() {
        return profile
    }

    void setProfile(String profile) {
        this.profile = profile
    }

    String getAppName() {
        return appName
    }

    void setAppName(String appName) {
        this.appName = appName
    }

    String getTeam() {
        return team
    }

    void setTeam(String team) {
        this.team = team
    }

    String getRepoBase() {
        return repoBase
    }

    void setRepoBase(String repoBase) {
        this.repoBase = repoBase
    }

    String getBranch() {
        return branch
    }

    void setBranch(String branch) {
        this.branch = branch
    }

    String getRepoCredId() {
        return repoCredId
    }

    void setRepoCredId(String repoCredId) {
        this.repoCredId = repoCredId
    }

    String getDeployRootPath() {
        return deployRootPath
    }

    void setDeployRootPath(String deployRootPath) {
        this.deployRootPath = deployRootPath
    }

    String getDeployServers() {
        return deployServers
    }

    void setDeployServers(String deployServers) {
        this.deployServers = deployServers
    }

    String getJavaOpts() {
        return javaOpts
    }

    void setJavaOpts(String javaOpts) {
        this.javaOpts = javaOpts
    }

    String getImageRegAddr() {
        return imageRegAddr
    }

    void setImageRegAddr(String imageRegAddr) {
        this.imageRegAddr = imageRegAddr
    }

    String getImageRegCredId() {
        return imageRegCredId
    }

    void setImageRegCredId(String imageRegCredId) {
        this.imageRegCredId = imageRegCredId
    }

    String getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version = version
    }
}
