package com.xpay.web.pms.web.vo.devops;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PublishVo {
    @NotBlank(message = "构建信息不能为空")
    private String buildMsg;
    @NotBlank(message = "部署项目不能为空")
    private String apps;
    @NotNull(message = "构建方式不能为空")
    private Integer buildType;
    private String notifyEmail = "";
    private String idc;

    public String getBuildMsg() {
        return buildMsg;
    }

    public void setBuildMsg(String buildMsg) {
        this.buildMsg = buildMsg;
    }

    public String getApps() {
        return apps;
    }

    public void setApps(String apps) {
        this.apps = apps;
    }

    public Integer getBuildType() {
        return buildType;
    }

    public void setBuildType(Integer buildType) {
        this.buildType = buildType;
    }

    public String getNotifyEmail() {
        return notifyEmail;
    }

    public void setNotifyEmail(String notifyEmail) {
        this.notifyEmail = notifyEmail;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }
}
