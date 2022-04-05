package com.xpay.facade.message.enums;

/**
 * 终端设备操作系统枚举
 */
public enum OSTypeEnum {
    ANDROID(1, "android"),
    IOS(2, "ios"),
    ;
    private int value;
    private String desc;

    private OSTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static OSTypeEnum getEnum(int value) {
        OSTypeEnum resultEnum = null;
        OSTypeEnum[] enumAry = OSTypeEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue() == value) {
                resultEnum = enumAry[i];
                break;
            }
        }
        return resultEnum;
    }
}
