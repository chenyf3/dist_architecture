package com.xpay.gateway.api.exceptions;

/**
 * @Description: 网关专用异常类
 * @author: chenyf
 * @Date: 2019/11/01
 */
public class GatewayException extends RuntimeException {
    private String respCode;
    private String respMsg;
    protected int errCode;

    private GatewayException() {
        super();
    }

    public GatewayException(String msg) {
        super(msg);
    }

    public GatewayException(String msg, Throwable t) {
        super(msg, t);
    }

    public static GatewayException fail(String respCode, String respMsg, int errCode) {
        GatewayException exception = new GatewayException(respCode + "," + respMsg + "," + errCode);
        exception.setRespCode(respCode);
        exception.setRespMsg(respMsg);
        exception.setErrCode(errCode);
        return exception;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String toMsg(){
        return "{respCode:" + respCode + ", respMsg:" + respMsg + ", errCode:" + errCode + "}";
    }
}
