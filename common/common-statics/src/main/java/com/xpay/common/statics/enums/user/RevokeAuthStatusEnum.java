package com.xpay.common.statics.enums.user;

public enum RevokeAuthStatusEnum {
    /**
     * 待处理
     **/
    PENDING(1, "待处理"),

    /**
     * 处理中
     **/
    PROCESSION(2, "处理中"),

    /**
     * 已完成
     **/
    FINISH(3, "已完成");


    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;


    RevokeAuthStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
