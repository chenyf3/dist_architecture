package com.xpay.service.accountmch.listeners;

import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.service.accountmch.biz.AccountAlertBiz;
import com.xpay.service.accountmch.biz.AccountScheduleBiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScheduleProcessListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AccountScheduleBiz accountScheduleBiz;
    @Autowired
    AccountAlertBiz accountAlertBiz;
    /**
     * 定时账务处理
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_SCHEDULE_PROCESS, concurrency = "1-1", subscription = "asyncAccountProcess")
    public void scheduleAccountProcess(String msg) {
        accountScheduleBiz.scanPendingAndDoAccountProcess();
    }

    /**
     * 定时账务结果回调
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_SCHEDULE_CALLBACK, concurrency = "1-1", subscription = "accountProcessCallback")
    public void scheduleProcessResultCallback(String msg) {
        accountScheduleBiz.scanPendingAndDoResultCallback();
    }

    /**
     * 定时合并账务处理
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_SCHEDULE_MERGE_PROCESS, concurrency = "1-1", subscription = "scheduleMergeProcess")
    public void scheduleMergeProcess(String msg) {
        accountScheduleBiz.scanPendingAndDoMergeProcess();
    }

    /**
     * 账务处理预警相关
     * @param msg
     */
    @JmsListener(destination = TopicDest.ACCOUNT_MCH_SCHEDULE_PROCESS_ALERT, concurrency = "1-2", subscription = "scheduleProcessAlert")
    public void scheduleAccountProcessAlert(String msg){
        MsgDto msgDto = JsonUtil.toBean(msg, MsgDto.class);
        Map<String, String> param = JsonUtil.toBean(msgDto.getJsonParam(), HashMap.class);
        String taskType = param.get("taskType");
        if("pendingLongAlert".equals(taskType)){
            accountAlertBiz.accountProcessPendingPendingLongAlert();
        }else if("processingLongAlert".equals(taskType)){
            accountAlertBiz.accountProcessPendingProcessingLongAlert();
        }else if("resultAuditAlert".equals(taskType)){
            accountAlertBiz.accountProcessResultAuditAlert();
        }
    }
}
