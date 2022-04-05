package com.xpay.web.api.common.enums;

/**
 * 短信发送类型
 */
public enum SmsType {
    LOGIN_CODE(1, "登录验证码"),
    RETRIEVE_LOGIN_PWD(2, "找回登录密码"),
    CHANGE_TRADE_PWD(4, "修改支付密码"),
    RESET_TRADE_PWD(3, "重置支付密码"),
    CHANGE_API_SEC_KEY(5, "修改API密钥"),
    CHANGE_IMPORTANT_INFO(6, "修改重要信息"),
    ;

    private int value;
    private String desc;

    private SmsType(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static SmsType getEnum(Integer value){
        SmsType[] values = SmsType.values();
        for(int i=0; i<values.length; i++){
            if(values[i].getValue() == value){
                return values[i];
            }
        }
        return null;
    }
}
