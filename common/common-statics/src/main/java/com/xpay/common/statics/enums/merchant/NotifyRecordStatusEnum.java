package com.xpay.common.statics.enums.merchant;

public enum NotifyRecordStatusEnum {
    SUCCESS(1, "成功"),
    FAIL(2, "失败"),
    ;

    private int value;
    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    private NotifyRecordStatusEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }
}
