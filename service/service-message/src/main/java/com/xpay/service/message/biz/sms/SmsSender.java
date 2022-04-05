package com.xpay.service.message.biz.sms;

import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;

import java.util.LinkedHashMap;

/**
 * 短信发送接口
 */
public interface SmsSender {

    /**
     * 短信前缀名称(短信签名)
     * @return
     */
    public String signName(String tplCode);

    /**
     * （若运营商没有提供此功能可在实现类中直接抛出异常）
     * 使用运营商短信模版发送短信
     * @param phone     手机号
     * @param tplCode   短信模板编号
     * @param tplParam  模板参数
     * @param signName  短信签名(可选，不传则使用默认的)
     * @param trxNo     业务流水号，可选
     * @return
     */
    public SmsRespDto send(String phone, String tplCode, LinkedHashMap<String, Object> tplParam, String signName, String trxNo);

    /**
     * 获取单条短信发送状态
     */
    public SmsQueryResp getSingleSmsStatus(SmsQueryParam queryParam);
}
