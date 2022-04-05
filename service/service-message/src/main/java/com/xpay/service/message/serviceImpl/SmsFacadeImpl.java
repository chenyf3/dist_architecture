package com.xpay.service.message.serviceImpl;

import com.xpay.facade.message.dto.SmsSendDto;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.enums.SmsTemplateEnum;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;
import com.xpay.facade.message.service.SmsFacade;
import com.xpay.service.message.biz.sms.SmsBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class SmsFacadeImpl implements SmsFacade {
    @Autowired
    SmsBiz smsBiz;

    /**
     * 发送短信码接口
     * @param phone        收信人手机号
     * @param code         短信码(验证码等)
     * @param tplName      短信模板名，参考：{@link SmsTemplateEnum}
     * @param trxNo        短信签名，可选
     * @param trxNo        业务流水号，可选
     * @return
     */
    @Override
    public SmsRespDto sendCode(String phone, String code, String tplName, String signName, String trxNo){
        return smsBiz.send(phone, code, tplName, signName, trxNo);
    }

    /**
     * 短信发送接口
     * @param smsParam
     * @return
     */
    @Override
    public SmsRespDto send(SmsSendDto smsParam){
        return smsBiz.send(smsParam);
    }

    @Override
    public SmsQueryResp query(SmsQueryParam queryParam){
        return smsBiz.query(queryParam);
    }
}
