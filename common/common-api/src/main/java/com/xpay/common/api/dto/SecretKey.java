package com.xpay.common.api.dto;

public class SecretKey {
    /**
     * 对请求参数进行签名的私钥
     */
    private String reqSignKey;
    /**
     * 对响应参数进行验签的公钥
     */
    private String respVerifyKey;
    /**
     * 对sec_key进行加密的公钥
     */
    private String secKeyEncryptKey;
    /**
     * 对sec_key进行解密的私钥
     */
    private String secKeyDecryptKey;

    public String getReqSignKey() {
        return reqSignKey;
    }

    public void setReqSignKey(String reqSignKey) {
        this.reqSignKey = reqSignKey;
    }

    public String getRespVerifyKey() {
        return respVerifyKey;
    }

    public void setRespVerifyKey(String respVerifyKey) {
        this.respVerifyKey = respVerifyKey;
    }

    public String getSecKeyEncryptKey() {
        return secKeyEncryptKey;
    }

    public void setSecKeyEncryptKey(String secKeyEncryptKey) {
        this.secKeyEncryptKey = secKeyEncryptKey;
    }

    public String getSecKeyDecryptKey() {
        return secKeyDecryptKey;
    }

    public void setSecKeyDecryptKey(String secKeyDecryptKey) {
        this.secKeyDecryptKey = secKeyDecryptKey;
    }
}
