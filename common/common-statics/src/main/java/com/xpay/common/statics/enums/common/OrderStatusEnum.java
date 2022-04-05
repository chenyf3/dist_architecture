package com.xpay.common.statics.enums.common;

public enum OrderStatusEnum {
    PENDING("01", "受理中"),
    PROCESSING("02", "处理中"),
    SUCCESS("03", "成功"),
    FAIL("04", "失败"),

    ;

    private String value;
    private String msg;

    public String getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }

    private OrderStatusEnum(String value, String msg){
        this.value = value;
        this.msg = msg;
    }
}
