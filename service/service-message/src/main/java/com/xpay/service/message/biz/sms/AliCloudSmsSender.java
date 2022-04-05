package com.xpay.service.message.biz.sms;

import com.alibaba.alicloud.sms.ISmsService;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.SmsRespDto;
import com.xpay.facade.message.params.SmsQueryParam;
import com.xpay.facade.message.params.SmsQueryResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * 阿里云短信平台发送器
 */
@Service
public class AliCloudSmsSender implements SmsSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public final static String SUCCESS_SEND_STATUS = "OK";
    public final static long SUCCESS_RECEIVE_STATUS = 3;
    @Autowired
    private ISmsService smsService;

    @Override
    public String signName(String tplCode) {
        return "none";//TODO 修改成默认的短信签名
    }

    /**
     * 发送短信
     * @param phone     手机号
     * @param tplCode   短信模板编号
     * @param tplParam  模板参数
     * @param signName  短信签名(可选，不传则使用默认的)
     * @param trxNo     业务流水号，可选
     * @return
     */
    @Override
    public SmsRespDto send(String phone, String tplCode, LinkedHashMap<String, Object> tplParam, String signName, String trxNo){
        // 组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        // 必填:待发送手机号
        request.setPhoneNumbers(phone);
        // 必填:短信签名-可在短信控制台中找到
        if(StringUtil.isEmpty(signName)) {
            signName = signName(tplCode);
        }
        request.setSignName(signName);
        // 必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(tplCode);
        request.setTemplateParam(JsonUtil.toJson(tplParam));
        if(StringUtil.isNotEmpty(trxNo)){
            request.setOutId(trxNo);
        }

        try {
            SmsRespDto resp = new SmsRespDto();
            SendSmsResponse sendResp = smsService.sendSmsRequest(request);
            resp.setCode(sendResp.getCode());
            resp.setMessage(sendResp.getMessage());
            resp.setSerialNo(sendResp.getBizId());
            resp.setIsSuccess(SUCCESS_SEND_STATUS.equalsIgnoreCase(sendResp.getCode()));

            if(! resp.getIsSuccess()){
                logger.error("短信发送失败 phone={} code={} message={}", phone, resp.getCode(), resp.getMessage());
            }
            return resp;
        } catch (ClientException e) {
            logger.error("短信发送异常 phone={}", phone, e);
            throw new BizException(BizException.UNEXPECT_ERROR, "短信发送异常", e);
        } catch (Exception e){
            logger.error("短信发送异常 phone={}", phone, e);
            throw e;
        }
    }

    /**
     * 获取单条短信发送状态
     */
    @Override
    public SmsQueryResp getSingleSmsStatus(SmsQueryParam queryParam){
        // 组装请求对象
        QuerySendDetailsRequest queryRequest = new QuerySendDetailsRequest();
        // 必填-号码
        queryRequest.setPhoneNumber(queryParam.getPhone());
        // 必填-短信发送的日期 支持30天内记录查询（可查其中一天的发送数据），格式yyyyMMdd
        queryRequest.setSendDate(DateUtil.formatCompactDate(DateUtil.convertDate(queryParam.getSendDate())));
        // 必填-页大小
        queryRequest.setPageSize(1L);
        // 必填-当前页码从1开始计数
        queryRequest.setCurrentPage(1L);
        queryRequest.setBizId(queryParam.getSerialNo());

        try {
            QuerySendDetailsResponse response = smsService.querySendDetails(queryRequest);
            SmsQueryResp resp = new SmsQueryResp();
            resp.setCode(response.getCode());
            resp.setMessage(response.getMessage());
            if(SUCCESS_SEND_STATUS.equals(response.getCode()) && response.getSmsSendDetailDTOs() != null && response.getSmsSendDetailDTOs().size() > 0){
                QuerySendDetailsResponse.SmsSendDetailDTO detailDTO = response.getSmsSendDetailDTOs().get(0);
                resp.setBizKey(detailDTO.getOutId());
                resp.setSendDate(detailDTO.getSendDate());
                resp.setReceiveDate(detailDTO.getReceiveDate());
                resp.setSendStatus(String.valueOf(detailDTO.getSendStatus()));
                resp.setIsSuccess(SUCCESS_RECEIVE_STATUS == detailDTO.getSendStatus());
            }
            return resp;
        } catch (ClientException e) {
            logger.error("查询短信发送结果异常 serialNo={} phone={}", queryParam.getSerialNo(), queryParam.getPhone(), e);
            throw new BizException(BizException.UNEXPECT_ERROR, "查询短信发送结果异常", e);
        } catch (Exception e){
            logger.error("查询短信发送结果异常 phone={}", queryParam.getPhone(), e);
            throw e;
        }
    }
}
