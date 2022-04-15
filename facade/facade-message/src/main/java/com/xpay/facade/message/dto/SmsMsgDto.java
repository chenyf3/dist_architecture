package com.xpay.facade.message.dto;

import com.xpay.common.statics.dto.mq.MsgDto;

import java.util.LinkedHashMap;

public class SmsMsgDto extends MsgDto {
    /**
     * 收信人手机号
     */
    private String phone;

    /**
     * 模版名称，使用 {@link com.xpay.facade.message.enums.SmsTemplateEnum} 枚举名称
     */
    private String tplName;

    /**
     * 模版中的参数
     */
    private LinkedHashMap<String, Object> tplParam;

    /**
     * 短信签名，可选，参考 {@link com.xpay.facade.message.enums.SignNameEnum}
     */
    private String signName;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTplName() {
        return tplName;
    }

    public void setTplName(String tplName) {
        this.tplName = tplName;
    }

    public LinkedHashMap<String, Object> getTplParam() {
        return tplParam;
    }

    public void setTplParam(LinkedHashMap<String, Object> tplParam) {
        this.tplParam = tplParam;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }
}
