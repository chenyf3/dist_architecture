package com.xpay.facade.message.params;

public class SmsQueryResp {
    private boolean isSuccess;//是否接收成功
    private String code;//响应码
    private String message;//描述
    private String sendStatus;//发送状态
    private String sendDate;//发送时间，格式 yyyy-MM-dd HH:mm:ss
    private String receiveDate;//接收时间，格式 yyyy-MM-dd HH:mm:ss
    private String bizKey;//业务编码

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }
}
