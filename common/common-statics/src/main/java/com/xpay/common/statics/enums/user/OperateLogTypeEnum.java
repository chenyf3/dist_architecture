package com.xpay.common.statics.enums.user;

/**
 * 操作类型
 */
public enum OperateLogTypeEnum {
    LOGIN(1, "登录"),
    LOGOUT(2, "退出"),
    CREATE(3, "添加"),
    MODIFY(4, "修改"),
    DELETE(5, "删除"),
    QUERY(6, "查询"),
    ;

    /**
     * 枚举值
     */
    private int value;
    /**
     * 描述
     */
    private String desc;


    OperateLogTypeEnum(int value, String desc) {
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
