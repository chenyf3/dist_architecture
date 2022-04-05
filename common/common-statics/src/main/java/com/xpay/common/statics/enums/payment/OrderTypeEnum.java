package com.xpay.common.statics.enums.payment;

public enum OrderTypeEnum {
    SINGLE(1, "单笔代付"),
    BATCH(2, "批量代付");

    /** 枚举值 */
    private int value;

    /** 描述 */
    private String desc;

    private OrderTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }
}
