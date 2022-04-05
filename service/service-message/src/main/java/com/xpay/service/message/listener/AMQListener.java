package com.xpay.service.message.listener;

import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.message.dto.EmailMsgDto;
import com.xpay.service.message.biz.email.EmailBiz;
import com.xpay.service.message.biz.mq.AmqTraceBiz;
import com.xpay.starter.amq.config.Const;
import com.xpay.starter.amq.tracer.TraceMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AMQListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EmailBiz emailBiz;
    @Autowired
    AmqTraceBiz amqTraceBiz;

    /**
     * 异步发送邮件的消息消费端
     * @param message
     */
    @JmsListener(destination = TopicDest.EMAIL_SEND_ASYNC, subscription = "emailAsyncConsume")
    public void emailAsyncConsume(String message) {
        EmailMsgDto msgDto = JsonUtil.toBean(message, EmailMsgDto.class);
        emailBiz.send(msgDto);
    }

    /**
     * ActiveMQ轨迹消息的消费端
     * @param msgStr
     */
    @JmsListener(destination = Const.TRACE_QUEUE_NAME, concurrency = "1-20")
    public void activemqTraceMsgConsume(String msgStr){
        try{
            TraceMsg msg = JsonUtil.toBean(msgStr, TraceMsg.class);
            amqTraceBiz.handleAmqTraceMsg(msg);
        }catch(Throwable e){
            //因为轨迹消息为非重要数据，有一定的数据丢失容忍性，所以可以接住异常，避免阻塞MQ消费而导致Broker负担加大
            logger.error("消费轨迹消息时出现异常 Exception = {} Message = {}", e.getMessage(), msgStr);
        }
    }
}
