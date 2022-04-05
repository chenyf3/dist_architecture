package com.xpay.service.message.test;

import com.xpay.facade.message.dto.SmsSendDto;
import com.xpay.facade.message.enums.SmsTemplateEnum;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;
import com.xpay.facade.message.service.SmsFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashMap;

public class SmsTest extends BaseTestCase {
    @DubboReference
    private SmsFacade smsFacade;
    private String phone = "13800138000";

    @Ignore
    @Test
    public void sendAliCloudLongTest(){
        SmsTemplateEnum tplPlatEnum = SmsTemplateEnum.REGISTER_CODE;

        SmsSendDto smsParam = new SmsSendDto();
        smsParam.setPhone(phone);
        smsParam.setTplName(tplPlatEnum.name());
        smsParam.setTrxNo("2222222222222222");

        LinkedHashMap<String, Object> tplParam = new LinkedHashMap<>();
        tplParam.put("realName", "张三");
        tplParam.put("notifyType", "登陆");
        tplParam.put("newPwd", "123456WE");
        smsParam.setTplParam(tplParam);
        SmsRespDto sendResp = smsFacade.send(smsParam);

        try{
            Thread.sleep(5000);//休眠5秒，等待短信被接收
        }catch(Exception e){
            e.printStackTrace();
        }

        SmsQueryParam queryParam = new SmsQueryParam();
        queryParam.setPlatform(tplPlatEnum.getPlat());
        queryParam.setPhone(phone);
        queryParam.setSendDate(DateUtil.formatDate(new Date()));
        queryParam.setSerialNo(sendResp.getSerialNo());
        SmsQueryResp queryResp = smsFacade.query(queryParam);
        System.out.println("queryResp = " + JsonUtil.toJson(queryResp));
    }

    @Ignore
    @Test
    public void sendAliCloudTest(){
        SmsTemplateEnum tplPlatEnum = SmsTemplateEnum.REGISTER_CODE;

        SmsSendDto smsParam = new SmsSendDto();
        smsParam.setPhone(phone);
        smsParam.setTplName(tplPlatEnum.name());
        smsParam.setTrxNo("2222222222222222");

        LinkedHashMap<String, Object> tplParam = new LinkedHashMap<>();
        tplParam.put("code", RandomUtil.getDigitStr(4));
        smsParam.setTplParam(tplParam);
        SmsRespDto sendResp = smsFacade.send(smsParam);

        try{
            Thread.sleep(5000);//休眠5秒，等待短信被接收
        }catch(Exception e){
            e.printStackTrace();
        }

        SmsQueryParam queryParam = new SmsQueryParam();
        queryParam.setPlatform(tplPlatEnum.getPlat());
        queryParam.setPhone(phone);
        queryParam.setSendDate(DateUtil.formatDate(new Date()));
        queryParam.setSerialNo(sendResp.getSerialNo());
        SmsQueryResp queryResp = smsFacade.query(queryParam);
        System.out.println("queryResp = " + JsonUtil.toJson(queryResp));
    }

    @Ignore
    @Test
    public void sendTencentTest(){
        SmsTemplateEnum tplPlatEnum = SmsTemplateEnum.CHANGE_API_SEC_KEY;

        SmsSendDto smsParam = new SmsSendDto();
        smsParam.setPhone(phone);
        smsParam.setTplName(tplPlatEnum.name());
        smsParam.setTrxNo("3333333333");

        LinkedHashMap<String, Object> tplParam = new LinkedHashMap<>();
        tplParam.put("balance", RandomUtil.getDigitStr(4));
        smsParam.setTplParam(tplParam);
        SmsRespDto sendResp = smsFacade.send(smsParam);
        System.out.println("sendResp = " + JsonUtil.toJson(sendResp));

        try{
            Thread.sleep(5000);//休眠5秒，等待短信被接收
        }catch(Exception e){
            e.printStackTrace();
        }

        SmsQueryParam queryParam = new SmsQueryParam();
        queryParam.setPlatform(tplPlatEnum.getPlat());
        queryParam.setPhone(phone);
        queryParam.setSendDate(DateUtil.formatDate(new Date()));
        queryParam.setSerialNo(sendResp.getSerialNo());
        SmsQueryResp queryResp = smsFacade.query(queryParam);
        System.out.println("queryResp = " + JsonUtil.toJson(queryResp));
    }
}
