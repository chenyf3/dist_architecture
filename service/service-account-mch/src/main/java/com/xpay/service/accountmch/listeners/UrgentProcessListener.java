package com.xpay.service.accountmch.listeners;

import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.accountmch.dto.AccountProcessBufferDto;
import com.xpay.service.accountmch.biz.AccountProcessHandler;
import com.xpay.service.accountmch.biz.AccountProcessResultBiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UrgentProcessListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AccountProcessHandler accountProcessHandler;
    @Autowired
    AccountProcessResultBiz accountProcessResultBiz;

    /**
     * 加急账务处理
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_URGENT_PROCESS, concurrency = "1-20", subscription = "urgentAccountProcess")
    public void urgentAccountProcess(String msg) {
        try {
            MsgDto msgDto = JsonUtil.toBean(msg, MsgDto.class);
            Map<String, Long> idMap = JsonUtil.toBean(msgDto.getJsonParam(), HashMap.class);
            Long processPendingId = idMap.get("processPendingId");

            accountProcessHandler.process(processPendingId);
        } catch (Exception e) {
            logger.error("加急账务处理出现异常 MsgDto = {} ", msg, e);
        }
    }

    /**
     * 加急账务结果回调
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_URGENT_CALLBACK, concurrency = "1-20", subscription = "urgentAccountProcessCallback")
    public void urgentProcessResultCallback(String msg) {
        try {
            MsgDto msgDto = JsonUtil.toBean(msg, MsgDto.class);
            Map<String, Long> idMap = JsonUtil.toBean(msgDto.getJsonParam(), HashMap.class);
            Long processResultId = idMap.get("processResultId");
            accountProcessResultBiz.sendProcessResultCallbackMsg(processResultId);
        } catch (Exception e) {
            logger.error("加急账务结果回调出现异常 MsgDto = {} ", msg, e);
        }
    }

    /**
     * 异步账务请求缓冲入库
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_PROCESS_BUFFER, concurrency = "1-20", subscription = "asyncProcessBufferSave")
    public void asyncProcessBufferSave(String msg){
        AccountProcessBufferDto bufferDto = JsonUtil.toBean(msg, AccountProcessBufferDto.class);
        AccountRequestDto requestDto = bufferDto.getRequestDto();
        List<AccountProcessDto> processDtoList = bufferDto.getProcessDtoList();
        boolean isSuccess = accountProcessHandler.saveAsync(requestDto, processDtoList);
        if(! isSuccess){
            throw new RuntimeException("缓冲异步账务入库失败");//抛出异常，让MQ重发消息
        }
    }
}
