package com.xpay.service.message.biz.sms;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.*;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;
import com.xpay.service.message.config.properties.TencentSmsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TencentSmsSender implements SmsSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String appId;
    private String signName;
    SmsClient smsClient;

    public TencentSmsSender(TencentSmsProperties properties){
        this.appId = properties.getAppId();
        this.signName = properties.getSignName();

        Credential credential = new Credential(properties.getSecretId(), properties.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setReqMethod("POST");
        httpProfile.setConnTimeout(properties.getConnTimeout());
        httpProfile.setEndpoint(properties.getUrl());

        ClientProfile clientProfile = new ClientProfile();
        /* SDK默认用TC3-HMAC-SHA256进行签名
         * 非必要请不要修改这个字段 */
        clientProfile.setSignMethod("HmacSHA256");
        clientProfile.setHttpProfile(httpProfile);
        this.smsClient = new SmsClient(credential, properties.getRegion(), clientProfile);
    }

    @Override
    public String signName(String tplCode) {
        return this.signName;
    }

    /**
     * https://cloud.tencent.com/document/product/382/43194#example
     * @param phone         手机号
     * @param tplCode       短信模版编码
     * @param tplParam      模版参数
     * @param trxNo         短信签名，可选
     * @param trxNo         业务流水号，可选
     * @return
     */
    @Override
    public SmsRespDto send(String phone, String tplCode, LinkedHashMap<String, Object> tplParam, String signName, String trxNo) {
        SendSmsRequest req = new SendSmsRequest();

        /* 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId，示例如1400006666 */
        req.setSmsSdkAppId(appId);

        /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名，签名信息可登录 [短信控制台] 查看 */
        if(StringUtil.isEmpty(signName)) {
            signName = signName(tplCode);
        }
        req.setSignName(signName);

        /* 国际/港澳台短信 SenderId: 国内短信填空，默认未开通，如需开通请联系 [sms helper] */
        String senderId = "";
        req.setSenderId(senderId);

        /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
        req.setSessionContext(trxNo);

        /* 短信号码扩展号: 默认未开通，如需开通请联系 [sms helper] */
        String extendCode = "";
        req.setExtendCode(extendCode);

        /* 模板 ID: 必须填写已审核通过的模板 ID。模板ID可登录 [短信控制台] 查看 */
        req.setTemplateId(tplCode);

        String[] phoneNumberSet = {"+86"+phone};
        req.setPhoneNumberSet(phoneNumberSet);

        /* 模板参数: 若无模板参数，则设置为空 */
        if(tplParam != null && !tplParam.isEmpty()){
            int i=0;
            String[] templateParamSet = new String[tplParam.size()];
            for(Map.Entry<String, Object> entry : tplParam.entrySet()){
                templateParamSet[i] = entry.getValue().toString();
                i++;
            }
            req.setTemplateParamSet(templateParamSet);
        }

        try {
            SendSmsResponse sendResp = smsClient.SendSms(req);

            SendStatus sendStatus = sendResp.getSendStatusSet()[0];

            SmsRespDto resp = new SmsRespDto();
            resp.setCode(sendStatus.getCode());
            resp.setMessage(sendStatus.getMessage());
            resp.setSerialNo(sendStatus.getSerialNo());
            resp.setIsSuccess("Ok".equalsIgnoreCase(sendStatus.getCode()));
            if(! resp.getIsSuccess()){
                logger.error("短信发送失败 phone={} code={} message={}", phone, resp.getCode(), resp.getMessage());
            }
            return resp;
        } catch (Exception e) {
            logger.error("短信发送异常 phone={}", phone, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public SmsQueryResp getSingleSmsStatus(SmsQueryParam queryParam) {
        PullSmsSendStatusByPhoneNumberRequest request = new PullSmsSendStatusByPhoneNumberRequest();

        Long limit = queryParam.getPageSize() == null ? 1L : Long.valueOf(queryParam.getPageSize());
        Long beginTime;
        if (StringUtil.isEmpty(queryParam.getSendDate())) {
            beginTime = (DateUtil.getDayStart(new Date()).getTime())/1000;
        } else {
            beginTime = (DateUtil.convertDate(queryParam.getSendDate()).getTime())/1000;
        }

        request.setSmsSdkAppId(appId);
        request.setPhoneNumber("+86" + queryParam.getPhone());
        request.setOffset(0L);//目前固定为0
        request.setLimit(limit);
        request.setBeginTime(beginTime);

        try {
            PullSmsSendStatusByPhoneNumberResponse response = smsClient.PullSmsSendStatusByPhoneNumber(request);

            SmsQueryResp resp = new SmsQueryResp();
            PullSmsSendStatus[] sendStatuses = response.getPullSmsSendStatusSet();
            if(sendStatuses == null || sendStatuses.length <= 0) {
                return resp;
            }

            for(PullSmsSendStatus sendStatus : sendStatuses){
                if(sendStatus.getSerialNo().equals(queryParam.getSerialNo())) {
                    resp.setIsSuccess("SUCCESS".equalsIgnoreCase(sendStatus.getReportStatus()));
                    resp.setCode(sendStatus.getReportStatus());
                    resp.setSendStatus(sendStatus.getReportStatus());
                    resp.setSendDate("");
                    if(sendStatus.getUserReceiveTime() != null && sendStatus.getUserReceiveTime() > 0) {
                        resp.setReceiveDate(DateUtil.formatDateTime(new Date(sendStatus.getUserReceiveTime())));
                    }
                    resp.setMessage(sendStatus.getDescription());
                    break;
                }
            }
            return resp;
        } catch (TencentCloudSDKException e) {
            logger.error("查询短信发送结果异常 serialNo={} phone={}", queryParam.getSerialNo(), queryParam.getPhone(), e);
            throw new BizException(BizException.UNEXPECT_ERROR, "查询短信发送结果异常", e);
        } catch (Exception e){
            logger.error("查询短信发送结果异常 serialNo={} phone={}", queryParam.getPhone(), e);
            throw e;
        }
    }
}
