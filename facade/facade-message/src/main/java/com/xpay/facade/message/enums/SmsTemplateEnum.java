package com.xpay.facade.message.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 运营商短信模版
 */
public enum SmsTemplateEnum {
    REGISTER_CODE(2, "xxxxxx", "注册验证码"),//[xxx]您正在注册成为新用户，验证码为${code}，感谢您的支持！
    LOGIN_CODE(2, "xxxxxx", "登录验证码"),//[xxx]您正在登录xxx系统，验证码为${code}，请勿泄露！
    RETRIEVE_PWD(2, "xxxxxx", "密码找回"),//[xxx]尊敬的用户，您正在进行密码找回操作，验证码为：${1}，请勿泄漏！
    CHANGE_TRADE_PWD_CODE(2, "xxxxxx", "修改支付密码"),//[xxx]您正在修改支付密码，验证码为：${code}，请勿泄漏！
    RESET_TRADE_PWD_CODE(2, "xxxxxx", "重置支付密码"),//[xxx]平台工作人员正为您发起重置支付密码，验证码为：${code}，请勿泄漏！
    PWD_RESET_NOTIFY(2, "xxxxxx", "密码重置通知"),//尊敬的${mchName}，您的${notifyType}密码已变更，请妥善保管！。
    CHANGE_API_SEC_KEY(2, "xxxxxx", "修改API密钥"),//[xxx]尊敬的用户，您正在修改api访问密钥，验证码为：${1}，请勿泄漏！
    CHANGE_IMPORTANT_INFO(2, "xxxxxx", "重要信息变更"),//尊敬的用户，您正在变更{1}，验证码为：${2}，{3}分钟有效，请勿泄漏！

    ;

    private Integer plat;//短信运营平台 1=阿里云 2=腾讯云
    private final String value;
    private final String desc;

    public final static int ALI_CLOUD = 1;
    public final static int TENCENT = 2;

    private SmsTemplateEnum(Integer platform, String value, String desc){
        this.plat = platform;
        this.value = value;
        this.desc = desc;
    }

    public Integer getPlat() {
        return plat;
    }

    public void setPlat(Integer plat) {
        this.plat = plat;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static SmsTemplateEnum getEnum(String name){
        return SmsTemplateEnum.valueOf(name);
    }

    public static Map<Integer, String> getPlatform(){
        Map<Integer, String> map = new HashMap<>();
        map.put(ALI_CLOUD, "阿里云");
        map.put(TENCENT, "腾讯云");
        return map;
    }
}
