package com.xpay.facade.mchnotify.dto;

public class NotifyLogDto {
    /**
     * http响应码
     */
    private int httpStatus;
    /**
     * http请求的错误信息
     */
    private String httpErrMsg;
    /**
     * 响应内容
     */
    private String respContent;
    /**
     * 验签结果
     */
    private String verifyResult;
    /**
     * 通知时间
     */
    private String notifyTime;
    /**
     * 当前通知次数
     */
    private int currTimes;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getHttpErrMsg() {
        return httpErrMsg;
    }

    public void setHttpErrMsg(String httpErrMsg) {
        this.httpErrMsg = httpErrMsg;
    }

    public String getRespContent() {
        return respContent;
    }

    public void setRespContent(String respContent) {
        this.respContent = respContent;
    }

    public String getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(String verifyResult) {
        this.verifyResult = verifyResult;
    }

    public String getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(String notifyTime) {
        this.notifyTime = notifyTime;
    }

    public int getCurrTimes() {
        return currTimes;
    }

    public void setCurrTimes(int currTimes) {
        this.currTimes = currTimes;
    }
}
