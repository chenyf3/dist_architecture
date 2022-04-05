package com.xpay.web.api.common.ddo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PwdResetDto {
    @NotNull(message = "请指定用户")
    private Long userId;
    @NotBlank(message = "请输入新密码")
    private String newPwd;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }
}
