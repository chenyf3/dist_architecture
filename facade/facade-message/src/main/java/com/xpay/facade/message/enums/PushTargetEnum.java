package com.xpay.facade.message.enums;

/**
 * 移动推送，推送目标枚举
 */
public enum PushTargetEnum {
    DEVICE(1, "根据设备推送"),
    ACCOUNT(2, "根据账号推送"),
    ALIAS(3, "根据别名推送"),
    TAG(4, "根据标签推送"),
    ALL(5, "推送给全部设备")
    ;

    private int value;
    private String desc;

    private PushTargetEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static PushTargetEnum getEnum(int value) {
        PushTargetEnum[] enumAry = PushTargetEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue() == value) {
                return enumAry[i];
            }
        }
        return null;
    }
}
