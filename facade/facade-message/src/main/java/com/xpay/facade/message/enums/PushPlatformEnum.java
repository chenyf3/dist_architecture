package com.xpay.facade.message.enums;

/**
 * 移动推送平台枚举
 */
public enum PushPlatformEnum {
    ALI_CLOUD(1, "阿里云"),
    TENCENT_XG(2, "腾讯信鸽"),

    ;
    private int value;
    private String desc;

    private PushPlatformEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static PushPlatformEnum getEnum(int value) {
        PushPlatformEnum resultEnum = null;
        PushPlatformEnum[] enumAry = PushPlatformEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue() == value) {
                resultEnum = enumAry[i];
                break;
            }
        }
        return resultEnum;
    }
}
