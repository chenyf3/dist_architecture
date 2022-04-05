package com.xpay.service.mchnotify.biz;

import com.xpay.common.api.dto.CallbackDto;
import com.xpay.common.api.dto.CallbackResp;
import com.xpay.common.api.dto.SecretKey;
import com.xpay.common.api.params.APIParam;
import com.xpay.common.api.params.MchInfo;
import com.xpay.common.api.params.CallbackParam;
import com.xpay.common.api.service.MchService;
import com.xpay.common.api.utils.CallbackUtil;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.mchnotify.dto.NotifyRecordDto;
import com.xpay.facade.mchnotify.dto.NotifyLogDto;
import com.xpay.common.statics.enums.merchant.NotifyRecordStatusEnum;
import com.xpay.starter.plugin.plugins.MQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class NotifyBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MchService mchService;
    @Autowired
    NotifyRecordBiz notifyRecordBiz;
    @Autowired
    MQSender mqSender;

    public void doNotifyAsync(CallbackDto callbackDto) {
        if(callbackDto.getNotifyTime() == null){
            callbackDto.setNotifyTime(DateUtil.formatDateTime(new Date()));
        }
        String url = callbackDto.getCallbackUrl();
        CallbackParam callbackParam = transferToCallbackParam(callbackDto);
        SecretKey secretKey = getSecretKey(callbackDto.getMchNo(), callbackDto.getSignType(), callbackDto.getVersion());

        CallbackUtil.Callback onCallback = new CallbackUtil.Callback() {
            @Override
            public void onResponse(CallbackResp response) {
                onNotifyResponse(callbackDto, response);
            }

            @Override
            public void onError(CallbackResp response, Throwable e) {
                onNotifyResponse(callbackDto, response);
            }
        };

        CallbackUtil.requestAsync(url, callbackParam, secretKey, onCallback);
    }

    public boolean doNotify(Long recordId, String operator) {
        NotifyRecordDto record = notifyRecordBiz.getNotifyRecord(recordId);
        if(record == null){
            throw new BizException(BizException.PARAM_INVALID, "记录不存在！");
        }

        logger.info("补发商户通知 mchNo={} trxNo={} mchTrxNo={} operator={}", record.getMchNo(), record.getTrxNo(), record.getMchTrxNo(), operator);
        CallbackDto callbackDto = JsonUtil.toBean(record.getOriMsg(), CallbackDto.class);
        callbackDto.setNotifyRecordId(record.getId());
        callbackDto.setNotifyTime(DateUtil.formatDateTime(new Date()));
        callbackDto.setCurrTimes(record.getCurrTimes() + 1);
        doNotifyAsync(callbackDto);
        return true;
    }

    private void onNotifyResponse(CallbackDto callbackDto, CallbackResp response) {
        if(response.isNeedRetry() && callbackDto.getCurrTimes() < callbackDto.getMaxTimes()){ //需要重试且当前重试次数小于最大重试次数
            NotifyRecordDto record = buildNotifyRecord(callbackDto, response, NotifyRecordStatusEnum.FAIL.getValue());
            notifyRecordBiz.insertOrAppendLog(record);

            int exponent = callbackDto.getCurrTimes() - 1;
            int delaySec = callbackDto.getBaseDelay()/1000 * (int)Math.pow(2, exponent);
            callbackDto.setNotifyRecordId(record.getId());
            callbackDto.setCurrTimes(callbackDto.getCurrTimes() + 1);
            logger.info("商户通知重试 mchNo={} trxNo={} mchTrxNo={} currTimes={}", record.getMchNo(), record.getTrxNo(), record.getMchTrxNo(), record.getCurrTimes());
            mqSender.sendOneDelayAsync(callbackDto, delaySec);
        }else{
            int recordStatus;
            if(response.isRespSuccess()){ //请求成功并且商户响应为通知成功
                recordStatus = NotifyRecordStatusEnum.SUCCESS.getValue();
            }else{
                recordStatus = NotifyRecordStatusEnum.FAIL.getValue();
                byte[] body = response.getBody();
                response.setBody(null);
                logger.info("商户通知不再重试 mchNo={} trxNo={} mchTrxNo={} currTimes={} maxTimes={} CallbackResp={} respBody={}",
                        callbackDto.getMchNo(), callbackDto.getTrxNo(), callbackDto.getMchTrxNo(), callbackDto.getCurrTimes(),
                        callbackDto.getMaxTimes(), JsonUtil.toJson(response), StringUtil.subLeft(new String(body, StandardCharsets.UTF_8), 2000));
                response.setBody(body);
            }
            NotifyRecordDto record = buildNotifyRecord(callbackDto, response, recordStatus);
            notifyRecordBiz.insertOrAppendLog(record);
        }
    }

    private NotifyRecordDto buildNotifyRecord(CallbackDto callbackDto, CallbackResp resp, int status){
        String bodyStr = resp.getBody() != null ? new String(resp.getBody(), StandardCharsets.UTF_8) : "";

        NotifyLogDto log = new NotifyLogDto();
        log.setNotifyTime(callbackDto.getNotifyTime());
        log.setCurrTimes(callbackDto.getCurrTimes());
        log.setVerifyResult(resp.getVerifyResult());
        log.setHttpStatus(resp.getHttpStatus());
        log.setHttpErrMsg(StringUtil.subLeft(resp.getHttpError(), 256));//截断响应内容，避免内容过长时浪费数据库存储空间
        log.setRespContent(StringUtil.subLeft(bodyStr, 256));//截断响应内容，避免用户不按规定返回响应内容时浪费数据库存储空间

        NotifyRecordDto record = new NotifyRecordDto();
        record.setId(callbackDto.getNotifyRecordId());
        record.setCreateTime(new Date());
        record.setMchNo(callbackDto.getMchNo() != null ? callbackDto.getMchNo() : "");
        record.setTrxNo(callbackDto.getTrxNo() != null ? callbackDto.getTrxNo() : "");
        record.setMchTrxNo(callbackDto.getMchTrxNo() != null ? callbackDto.getMchTrxNo() : "");
        record.setUrl(StringUtil.subLeft(callbackDto.getCallbackUrl(), 300));
        record.setStatus(status);
        record.setCurrTimes(callbackDto.getCurrTimes());
        record.setProductType(callbackDto.getProductType());
        record.setProductCode(callbackDto.getProductCode());
        record.setOriMsg(JsonUtil.toJson(callbackDto));
        record.setLog(log);
        return record;
    }

    private CallbackParam transferToCallbackParam(CallbackDto callbackDto) {
        CallbackParam callbackParam = new CallbackParam();
        callbackParam.setMchNo(callbackDto.getMchNo());
        callbackParam.setSignType(callbackDto.getSignType());
        callbackParam.setSecKey(callbackDto.getSecKey());
        callbackParam.setData(callbackDto.getData());
        callbackParam.setRandStr(RandomUtil.get32LenStr());
        return callbackParam;
    }

    private SecretKey getSecretKey(String appId, String signType, String version) {
        MchInfo mchInfo = mchService.getMchInfo(appId, new APIParam(signType, version));
        SecretKey secretKey = new SecretKey();
        secretKey.setReqSignKey(mchInfo.getSignGenKey());
        secretKey.setRespVerifyKey(mchInfo.getSignValidKey());
        secretKey.setSecKeyEncryptKey(mchInfo.getSecKeyEncryptKey());
        secretKey.setSecKeyDecryptKey(mchInfo.getSecKeyDecryptKey());
        return secretKey;
    }
}
