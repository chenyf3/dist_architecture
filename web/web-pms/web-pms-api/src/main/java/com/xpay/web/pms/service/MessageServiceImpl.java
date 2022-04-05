package com.xpay.web.pms.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.message.dto.EmailSendDto;
import com.xpay.facade.message.dto.SmsSendDto;
import com.xpay.facade.message.enums.SmsTemplateEnum;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.facade.message.service.SmsFacade;
import com.xpay.web.api.common.ddo.dto.EmailParamDto;
import com.xpay.web.api.common.ddo.dto.SmsParamDto;
import com.xpay.web.api.common.enums.SmsType;
import com.xpay.web.api.common.service.MessageService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @DubboReference
    EmailFacade emailFacade;
    @DubboReference
    SmsFacade smsFacade;

    @Override
    public boolean sendSmsCode(String phone, String code, Integer smsType) {
        SmsRespDto smsSendResp;
        if(smsType == SmsType.LOGIN_CODE.getValue()){
            smsSendResp = smsFacade.sendCode(phone, code, SmsTemplateEnum.LOGIN_CODE.name(), null, "");
        }else if(smsType == SmsType.RETRIEVE_LOGIN_PWD.getValue()){
            smsSendResp = smsFacade.sendCode(phone, code, SmsTemplateEnum.RETRIEVE_PWD.name(), null,"");
        }else if(smsType == SmsType.RESET_TRADE_PWD.getValue()){
            smsSendResp = smsFacade.sendCode(phone, code, SmsTemplateEnum.RESET_TRADE_PWD_CODE.name(), null,"");
        }else{
            throw new BizException("未支持的短信验证码类型！");
        }
        return smsSendResp.getIsSuccess();
    }

    @Override
    public boolean sendSms(SmsParamDto smsParam){
        SmsSendDto smsSendDto = BeanUtil.newAndCopy(smsParam, SmsSendDto.class);
        SmsRespDto respDto = smsFacade.send(smsSendDto);
        return respDto.getIsSuccess();
    }

    @Override
    public boolean sendEmailText(String from, String to, String[] cc, String subject, String content) {
        return emailFacade.send(from, to, cc, subject, content);
    }

    @Override
    public boolean sendEmailHtml(String from, String to, String[] cc, String subject, String content) {
        return emailFacade.sendHtml(from, to, cc, subject, content);
    }

    @Override
    public boolean sendEmailAsync(EmailParamDto param) {
        EmailSendDto emailParam = BeanUtil.newAndCopy(param, EmailSendDto.class);
        return emailFacade.sendAsync(emailParam);
    }
}
