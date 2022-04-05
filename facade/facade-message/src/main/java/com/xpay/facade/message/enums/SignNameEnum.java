package com.xpay.facade.message.enums;

/**
 * 短信签名枚举类
 */
public enum SignNameEnum {

    ;

    private String value;

    private SignNameEnum(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
