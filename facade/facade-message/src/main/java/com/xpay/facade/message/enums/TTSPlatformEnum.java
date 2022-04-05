package com.xpay.facade.message.enums;

/**
 * TTS转换平台枚举
 */
public enum TTSPlatformEnum {
    ALI_CLOUD(1, "阿里云"),
    TENCENT(2, "腾讯云"),

    ;
    private int value;
    private String desc;

    private TTSPlatformEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static TTSPlatformEnum getEnum(int value) {
        TTSPlatformEnum[] enumAry = TTSPlatformEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue() == value) {
                return enumAry[i];
            }
        }
        return null;
    }
}
