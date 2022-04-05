package com.xpay.common.statics.enums.common;

import java.util.Arrays;

/**
 * 签名算法类型
 */
public enum SignTypeEnum {
    MD5(1, "MD5"),
    RSA(2, "RSA2"),
    SM2(3, "SM2"),

    ;

    /**
     * 枚举值
     */
    private int value;
    /**
     * 描述
     */
    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    SignTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SignTypeEnum getEnum(int value) {
        return Arrays.stream(values()).filter(p -> p.value == value).findFirst().orElse(null);
    }
}
