package com.xpay.common.statics.enums.user;

import java.util.Arrays;
import java.util.Objects;

/**
 * 角色类型
 */
public enum RoleTypeEnum {
    /**
     * 管理员角色
     **/
    ADMIN(1, "管理员角色"),

    /**
     * 普通角色
     **/
    USER(2, "普通角色");


    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;


    RoleTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }


    public static RoleTypeEnum getEnum(int value) {
        return Arrays.stream(values()).filter(p -> Objects.equals(value, p.getValue())).findFirst().orElse(null);
    }

}
