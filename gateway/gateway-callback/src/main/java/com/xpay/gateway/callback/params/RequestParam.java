package com.xpay.gateway.callback.params;

import com.xpay.gateway.callback.enums.CompanyEnum;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调请求参数
 * @author chenyf
 */
public class RequestParam {
    /**
     * 回调请求的IP
     */
    private String ip;
    /**
     * 回调请求的路径
     */
    private String path;
    /**
     * 回调的请求方式：POST/GET
     */
    private String method;
    /**
     * 回调公司名称
     */
    private CompanyEnum company;
    /**
     * 回调请求头
     */
    private Map<String, String> headers = new HashMap<>();
    /**
     * 签名串
     */
    private String signature;
    /**
     * 需要执行验签的内容
     */
    private byte[] signBody;
    /**
     * 请求体数据
     */
    private byte[] body;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public CompanyEnum getCompany() {
        return company;
    }

    public void setCompany(CompanyEnum company) {
        this.company = company;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public byte[] getSignBody() {
        return signBody;
    }

    public void setSignBody(byte[] signBody) {
        this.signBody = signBody;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"ip\":").append(getIp()).append(", ")
                .append("\"path\":").append(getPath()).append(", ")
                .append("\"method\":").append(getMethod()).append(", ")
                .append("\"company\":").append(getCompany()).append(", ")
                .append("\"signature\":").append(getSignature()).append(", ")
                .append("\"headers\":").append(getHeaders().toString()).append(", ")
                .append("\"body\":").append(new String(getBody(), StandardCharsets.UTF_8))
                .append("}");
        return builder.toString();
    }
}
