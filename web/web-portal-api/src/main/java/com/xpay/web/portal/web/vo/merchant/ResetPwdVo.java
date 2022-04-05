package com.xpay.web.portal.web.vo.merchant;

import jakarta.validation.constraints.NotNull;

public class ResetPwdVo {
    @NotNull(message="验证码不能为空")
    private String verifyCode;
    @NotNull(message="密码不能为空")
    private String newPwd;
    @NotNull(message="确认密码不能为空")
    private String confirmPwd;

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
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
