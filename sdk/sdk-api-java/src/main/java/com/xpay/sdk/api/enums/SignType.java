package com.xpay.sdk.api.enums;

/**
 * 签名类型枚举
 */
public enum SignType {
    MD5("1"),//MD5签名
    RSA("2"),//RSA2签名
    SM2("3"),//SM2国密
    ;

    /** 枚举值 */
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private SignType(String value) {
        this.value = value;
    }
}
