package com.xpay.facade.message.enums;

public enum EmailSendStatusEnum {
    PENDING(1, "待发送"),
    SENDING(2, "发送中"),
    FINISH(3, "已发送"),
    ;

    private int value;
    private String desc;

    private EmailSendStatusEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static EmailSendStatusEnum getEnum(int value) {
        EmailSendStatusEnum resultEnum = null;
        EmailSendStatusEnum[] enumAry = EmailSendStatusEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue() == value) {
                resultEnum = enumAry[i];
                break;
            }
        }
        return resultEnum;
    }
}
