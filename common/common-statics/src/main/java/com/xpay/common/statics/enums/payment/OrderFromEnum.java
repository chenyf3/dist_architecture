package com.xpay.common.statics.enums.payment;

public enum OrderFromEnum {
    API(1, "API接口"),
    PORTAL(2, "商户后台");

    /** 枚举值 */
    private int value;

    /** 描述 */
    private String desc;

    private OrderFromEnum(int value, String desc) {
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
