package com.xpay.facade.message.service;

import com.xpay.facade.message.dto.SmsSendDto;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.enums.SmsTemplateEnum;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;

/**
 * 短信发送接口
 */
public interface SmsFacade {

    /**
     * 发送短信码接口
     * @param phone        收信人手机号
     * @param code         短信码(验证码等)
     * @param tplName      短信模板名 {@link SmsTemplateEnum}
     * @param trxNo        短信签名，可选
     * @param trxNo        业务流水号，可选
     * @return
     */
    public SmsRespDto sendCode(String phone, String code, String tplName, String signName, String trxNo);

    /**
     * 短信发送接口
     * @param smsParam
     * @return
     */
    public SmsRespDto send(SmsSendDto smsParam);

    /**
     * 查询短信发送结果
     * @param queryParam
     * @return
     */
    public SmsQueryResp query(SmsQueryParam queryParam);
}
