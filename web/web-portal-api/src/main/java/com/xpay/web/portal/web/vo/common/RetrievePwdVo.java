package com.xpay.web.portal.web.vo.common;

import jakarta.validation.constraints.NotBlank;

/**
 * 找回密码
 */
public class RetrievePwdVo {
    @NotBlank(message = "用户名不能为空")
    private String loginName;
    @NotBlank(message = "验证码不能为空")
    private String code;
    @NotBlank(message = "密码不能为空")
    private String newPwd;
    @NotBlank(message = "确认密码不能为空")
    private String confirmPwd;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
