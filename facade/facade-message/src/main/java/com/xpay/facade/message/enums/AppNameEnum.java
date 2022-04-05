package com.xpay.facade.message.enums;

/**
 * 应用名枚举
 */
public enum AppNameEnum {
    PDA_CASHIER(1, "手持收银系统"),
    PDA_CASHIER_LITE(2, "轻量化手持收银系统"),
    DESKTOP_CASHIER(3, "台式收银系统")

    ;

    private int value;
    private String desc;

    public int getValue() {
        return value;
    }
    public String getDesc() {
        return desc;
    }

    private AppNameEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static AppNameEnum getEnum(Integer value){
        AppNameEnum resultEnum = null;
        AppNameEnum[] enumAry = AppNameEnum.values();
        for (AppNameEnum typeEnum : enumAry) {
            if (typeEnum.getValue() == value) {
                resultEnum = typeEnum;
                break;
            }
        }
        return resultEnum;
    }
}
