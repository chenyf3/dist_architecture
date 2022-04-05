package com.xpay.facade.base.config.enums;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/19
 */
public enum DistLockStatusEnum {
    FREE(1, "空闲"),
    LOCKING(2, "锁定");

    private int value;
    private String desc;

    DistLockStatusEnum(int value, String desc) {
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
