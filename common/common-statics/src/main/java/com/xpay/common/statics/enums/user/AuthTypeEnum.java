package com.xpay.common.statics.enums.user;

/**
 * 权限类型枚举
 * @date 2019/11/1
 */
public enum AuthTypeEnum {

    /**
     * 菜单项
     */
    MENU_TYPE(1, "菜单项"),

    /**
     * 功能项
     */
    ACTION_TYPE(2, "功能项"),

    ;

    /**
     * 枚举值
     */
    private int value;
    /**
     * 描述
     */
    private String desc;


    AuthTypeEnum(int value, String desc) {
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
