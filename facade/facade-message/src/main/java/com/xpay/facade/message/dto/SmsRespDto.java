package com.xpay.facade.message.dto;

import java.io.Serializable;

/**
 * 发送短信响应体
 */
public class SmsRespDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isSuccess;//是否发送成功
    private String code;//响应码
    private String serialNo;//平台返回的发送流水号，查询发送结果时使用
    private String message;//描述

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean success) {
        isSuccess = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
