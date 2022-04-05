package com.xpay.web.portal.service;

import com.xpay.common.utils.BeanUtil;
import com.xpay.common.statics.exception.BizException;
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
    SmsFacade smsFacade;
    @DubboReference
    EmailFacade emailFacade;

    @Override
    public boolean sendSmsCode(String phone, String code, Integer smsType) {
        SmsTemplateEnum tplPlatEnum;
        if(smsType == SmsType.RETRIEVE_LOGIN_PWD.getValue()){
            tplPlatEnum = SmsTemplateEnum.RETRIEVE_PWD;
        }else if(smsType == SmsType.CHANGE_TRADE_PWD.getValue()){
            tplPlatEnum = SmsTemplateEnum.RESET_TRADE_PWD_CODE;
        }else if(smsType == SmsType.CHANGE_API_SEC_KEY.getValue()){
            tplPlatEnum = SmsTemplateEnum.CHANGE_API_SEC_KEY;
        }else{
            throw new BizException("暂不支持此类型的短信验证码");
        }

        SmsRespDto sendResp = smsFacade.sendCode(phone, code, tplPlatEnum.name(), null, "");
        if(! sendResp.getIsSuccess()){
            throw new BizException("验证码发送失败，" + sendResp.getMessage());
        }
        return true;
    }

    @Override
    public boolean sendSms(SmsParamDto smsParam){
        SmsSendDto smsSendDto = BeanUtil.newAndCopy(smsParam, SmsSendDto.class);
        SmsRespDto respDto = smsFacade.send(smsSendDto);
        return respDto.getIsSuccess();
    }

    @Override
    public boolean sendEmailText(String from, String to, String[] cc, String subject, String content){
        return emailFacade.send(from, to, cc, subject, content);
    }

    @Override
    public boolean sendEmailHtml(String from, String to, String[] cc, String subject, String content){
        return emailFacade.sendHtml(from, to, cc, subject, content);
    }

    @Override
    public boolean sendEmailAsync(EmailParamDto param){
        EmailSendDto emailParam = BeanUtil.newAndCopy(param, EmailSendDto.class);
        return emailFacade.sendAsync(emailParam);
    }
}
