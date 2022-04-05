package com.xpay.dto

/**
 * 项目发布参数
 */
class PublishDto {
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
     * 子目录
     */
    private String subPath
    /**
     * 机房编号
     */
    private String idc
    /**
     * k8s访问地址
     */
    private String k8sUrl
    /**
     * k8s访问凭证
     */
    private String k8sCredId
    /**
     * k8s中访问私有镜像仓库的密钥名称
     */
    private String k8sRegSecret

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

    String getSubPath() {
        return subPath
    }

    void setSubPath(String subPath) {
        this.subPath = subPath
    }

    String getIdc() {
        return idc
    }

    void setIdc(String idc) {
        this.idc = idc
    }

    String getK8sUrl() {
        return k8sUrl
    }

    void setK8sUrl(String k8sUrl) {
        this.k8sUrl = k8sUrl
    }

    String getK8sCredId() {
        return k8sCredId
    }

    void setK8sCredId(String k8sCredId) {
        this.k8sCredId = k8sCredId
    }

    String getK8sRegSecret() {
        return k8sRegSecret
    }

    void setK8sRegSecret(String k8sRegSecret) {
        this.k8sRegSecret = k8sRegSecret
    }
}