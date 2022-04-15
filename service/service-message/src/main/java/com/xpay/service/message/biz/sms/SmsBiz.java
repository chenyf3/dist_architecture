package com.xpay.service.message.biz.sms;

import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.SmsMsgDto;
import com.xpay.facade.message.dto.SmsSendDto;
import com.xpay.facade.message.enums.SmsTemplateEnum;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;
import com.xpay.service.message.biz.common.TemplateResolver;
import com.xpay.starter.plugin.plugins.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

/**
 * 短信发送逻辑层
 */
@Component
public class SmsBiz {
    @Autowired
    TemplateResolver templateResolver;
    @Autowired
    AliCloudSmsSender aliCloudSmsSender;
    @Autowired
    TencentSmsSender tencentSmsSender;
    @Autowired
    MQSender mqSender;

    public SmsRespDto send(String phone, String code, String tplName, String signName, String trxNo) {
        if (StringUtil.isEmpty(code)) {
            throw new BizException("短信码不能为空");
        }

        LinkedHashMap<String, Object> tplParam = new LinkedHashMap();
        tplParam.put("code", code);//固定使用此参数名，在短信模板中设置参数名的时候也要用这个

        SmsSendDto smsParam = new SmsSendDto();
        smsParam.setPhone(phone);
        smsParam.setTplName(tplName);
        smsParam.setTplParam(tplParam);
        smsParam.setSignName(signName);
        smsParam.setTrxNo(trxNo);
        return send(smsParam);
    }

    public SmsRespDto send(SmsSendDto smsParam) {
        if (smsParam == null) {
            throw new BizException("短信发送参数不能为空");
        } else if (StringUtil.isEmpty(smsParam.getPhone())) {
            throw new BizException("手机号不能为空");
        } else if (StringUtil.isEmpty(smsParam.getTplName())) {
            throw new BizException("短信模板名称不能为空");
        }

        SmsTemplateEnum smsTemplate = getSmsTemplate(smsParam.getTplName());
        SmsSender smsSender = getSmsSender(smsTemplate.getPlat());
        String tplCode = smsTemplate.getValue();
        return smsSender.send(smsParam.getPhone(), tplCode, smsParam.getTplParam(), smsParam.getSignName(), smsParam.getTrxNo());
    }

    public boolean sendAsync(SmsSendDto smsParam) {
        if (smsParam == null) {
            throw new BizException("短信发送参数不能为空");
        } else if (StringUtil.isEmpty(smsParam.getPhone())) {
            throw new BizException("手机号不能为空");
        } else if (StringUtil.isEmpty(smsParam.getTplName())) {
            throw new BizException("短信模板名称不能为空");
        }

        SmsMsgDto msgDto = BeanUtil.newAndCopy(smsParam, SmsMsgDto.class);
        msgDto.setTopic(TopicDest.SMS_SEND_ASYNC);
        msgDto.setTags(TopicGroup.COMMON_GROUP);
        return mqSender.sendOne(msgDto);
    }

    public SmsQueryResp query(SmsQueryParam queryParam) {
        SmsSender smsSender = getSmsSender(queryParam.getPlatform());
        return smsSender.getSingleSmsStatus(queryParam);
    }

    private SmsTemplateEnum getSmsTemplate(String tplName) {
        return SmsTemplateEnum.getEnum(tplName);
    }

    private SmsSender getSmsSender(Integer platform) {
        //此处允许为null的情况，相当于使用了一个默认的短信服务平台，如果后续需要更换短信运营商，可以直接修改此处，而不需要修改调用端的代码
        if(platform == null || platform == SmsTemplateEnum.TENCENT){
            return tencentSmsSender;
        }else if(platform == SmsTemplateEnum.ALI_CLOUD){
            return aliCloudSmsSender;
        }
        throw new BizException(BizException.BIZ_INVALID, "未预期的短信运营商: " + platform);
    }
}
