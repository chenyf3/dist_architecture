package com.xpay.facade.extend.enums;

public enum BuildStatusEnum {
    PENDING(1, "待处理"),
    QUEUEING(2, "排队中"),
    PROCESSING(3, "处理中"),
    SUCCESS(4, "成功"),
    FAILURE(5, "失败"),
    UNSTABLE(6, "不稳定"),
    ABORT(7, "已取消"),
    TIMEOUT(8, "已超时"),
    ;

    private int value;
    private String desc;

    private BuildStatusEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
