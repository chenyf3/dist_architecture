package com.xpay.web.api.common.ddo.dto;

import jakarta.validation.constraints.NotEmpty;

public class PwdChangeDto {
    @NotEmpty(message = "请输入旧密码")
    private String oldPwd;
    @NotEmpty(message = "请输入新密码")
    private String newPwd;
    @NotEmpty(message = "请确认新密码")
    private String confirmPwd;

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }
}
