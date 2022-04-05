package com.xpay.common.statics.enums.user;

public enum RevokeAuthTypeEnum {
    /**
     * 调整角色权限
     **/
    CHANGE_ROLE_AUTH(1, "调整角色权限"),

    /**
     * 取消角色关联
     **/
    CANCEL_ROLE_RELATE(2, "取消角色关联"),

    /**
     * 删除角色
     **/
    DELETE_ROLE(3, "删除角色");


    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;


    RevokeAuthTypeEnum(int value, String desc) {
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
