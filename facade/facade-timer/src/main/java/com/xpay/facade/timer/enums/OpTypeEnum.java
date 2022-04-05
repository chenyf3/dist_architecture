package com.xpay.facade.timer.enums;

public enum OpTypeEnum {
    ADD(1, "新增"),
    EDIT(2, "修改"),
    DEL(3, "删除"),
    PAUSE(4, "暂停"),//挂起
    RESUME(5, "恢复"),
    EXE(6, "触发"),
    NOTIFY(7, "通知"),
    ;


    private int value;
    private String desc;

    private OpTypeEnum(int value, String desc){
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
