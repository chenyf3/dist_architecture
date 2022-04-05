package com.xpay.gateway.callback.exceptions;

import com.xpay.gateway.callback.enums.CompanyEnum;

/**
 * @Description: 网关专用异常类
 * @author: chenyf
 */
public class GatewayException extends RuntimeException {
    //公司名称
    private CompanyEnum company;
    //请求路径
    private String path;
    //错误码
    private String code;
    //错误描述
    private String msg;

    public GatewayException(String msg) {
        super(msg);
    }

    public GatewayException(String msg, Throwable t) {
        super(msg, t);
    }

    public static GatewayException fail(CompanyEnum company, String path, String code, String msg) {
        GatewayException exception = new GatewayException(company + "," + path + "," + code + "," + msg);
        exception.setCompany(company);
        exception.setPath(path);
        exception.setCode(code);
        exception.setMsg(msg);
        return exception;
    }

    public CompanyEnum getCompany() {
        return company;
    }

    public void setCompany(CompanyEnum company) {
        this.company = company;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toMsg(){
        return "{path: " + path + ", code:" + code + ", msg:" + msg + "}";
    }
}
