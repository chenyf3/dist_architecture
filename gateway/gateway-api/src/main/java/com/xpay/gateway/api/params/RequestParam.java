package com.xpay.gateway.api.params;

/**
 * 用户的请求参数
 * @author chenyf
 * @date 2018-12-15
 */
public class RequestParam {
    private String method;
    private String version;
    private Object data;
    private String randStr;
    private String signType;
    private String mchNo;
    private String secKey;
    private String timestamp;//时间戳，精确倒毫秒

    /**------------- 以下是临时存放的字段，只在网关内部使用 ---------------*/
    private String signature;//源签名串
    private byte[] signBody;//需要执行验签的请求体

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public void setTempField(String signature, byte[] signBody){
        this.signature = signature;
        this.signBody = signBody;
    }

    public void clearTempField(){
        this.signature = null;
        this.signBody = null;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"method\":").append(getMethod()).append(", ")
                .append("\"version\":").append(getVersion()).append(", ")
                .append("\"randStr\":").append(getRandStr()).append(", ")
                .append("\"signType\":").append(getSignType()).append(", ")
                .append("\"mchNo\":").append(getMchNo()).append(", ")
                .append("\"secKey\":").append(getSecKey()).append(", ")
                .append("\"timestamp\":").append(getTimestamp()).append(", ")
                .append("\"data:\"").append(getData().toString())
                .append("}");
        return builder.toString();
    }
}
