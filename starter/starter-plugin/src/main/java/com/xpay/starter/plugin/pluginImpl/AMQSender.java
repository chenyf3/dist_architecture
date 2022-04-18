package com.xpay.starter.plugin.pluginImpl;

import com.xpay.common.statics.constants.common.LogMarker;
import com.xpay.common.statics.constants.common.RemoteLogger;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.starter.plugin.consts.MQHeaders;
import com.xpay.starter.plugin.plugins.MQSender;
import com.xpay.starter.plugin.util.Utils;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * ActiveMQ消息发送器
 * @author chenyf
 */
public class AMQSender implements MQSender {
    private final static int SEND_TOO_LONG = 2500;//发送消息耗时太长的临界值
    public final static String FLAG = "ActiveMQSendFail";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Logger remoteLogger = LoggerFactory.getLogger(RemoteLogger.FAILOVER);
    private JmsTemplate jmsTemplate;

    public AMQSender(JmsTemplate jmsTemplate){
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * 发送单个消息
     * @param msg
     * @return
     */
    @Override
    public boolean sendOne(MsgDto msg) {
        long start = System.currentTimeMillis();
        Destination destination = isVirtualTopic(msg.getTopic()) ? new ActiveMQTopic(msg.getTopic()) : new ActiveMQQueue(msg.getTopic());

        try {
            jmsTemplate.send(destination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return buildTextMessage(session, msg);
                }
            });
        } catch (Exception e) {
            remoteLogger.error(MarkerFactory.getMarker(LogMarker.ACTIVEMQ_SEND_FAIL), Utils.toJson(msg), e);
        }

        printSendLongTime(start);
        return true;
    }

    @Override
    public void sendOne(MsgDto msg, Consumer<MsgDto> onFail) {
        boolean isOk = false;
        try{
            isOk = sendOne(msg);
        }catch(Throwable e){
            msg.setCause(e);
        }
        if(! isOk && onFail != null){
            onFail.accept(msg);
        }
    }

    @Override
    public boolean sendOne(String destination, String body, Map<String, String> header){
        long start = System.currentTimeMillis();
        Destination destinationObj = isVirtualTopic(destination) ? new ActiveMQTopic(destination) : new ActiveMQQueue(destination);

        try {
            jmsTemplate.send(destinationObj, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    Message message = session.createTextMessage(body);
                    transferHeader(message, header);
                    return message;
                }
            });
        } catch (Exception e) {
            remoteLogger.error(MarkerFactory.getMarker(LogMarker.ACTIVEMQ_SEND_FAIL), body, e);
        }

        printSendLongTime(start);
        return true;
    }

    public boolean sendOneDelay(MsgDto msg, int delaySec){
        if(msg.getHeader() == null){
            msg.setHeader(new HashMap<>());
        }
        msg.getHeader().put(MQHeaders.SCHEDULED_DELAY, String.valueOf(delaySec * 1000));
        return sendOne(msg);
    }

    public boolean sendOrderly(MsgDto msg, String hashKey, long timeout){
        throw new RuntimeException("Not Support!");
    }

    @Override
    public void sendOneWay(MsgDto msg) {
        sendOneAsync(msg, null);
    }

    @Override
    public void sendOneAsync(MsgDto msg, Consumer<MsgDto> callback) {
        CompletableFuture.runAsync(() -> {
            try{
                sendOne(msg);
            }catch(Throwable e){
                msg.setCause(e);
            }
            if(callback != null){
                callback.accept(msg);
            }
        });
    }

    @Override
    public void sendOneDelayAsync(MsgDto msg, int delaySec){
        CompletableFuture.runAsync(() -> sendOneDelay(msg, delaySec));
    }

    @Override
    public boolean sendBatch(String destination, List<? extends MsgDto> msgList) {
        throw new RuntimeException("Not Support!");
    }

    @Override
    public boolean sendBatch(List<? extends MsgDto> msgList) {
        throw new RuntimeException("Not Support!");
    }

    @Override
    public boolean sendTrans(MsgDto msg) {
        throw new RuntimeException("Not Support!");
    }

    /**
     * 送消息并同步等待响应数据
     * @param msg
     * @return
     */
    @Override
    public MsgDto sendAndReceive(MsgDto msg) {
        long start = System.currentTimeMillis();
        Destination destination = isVirtualTopic(msg.getTopic()) ? new ActiveMQTopic(msg.getTopic()) : new ActiveMQQueue(msg.getTopic());

        Message respMsg = jmsTemplate.sendAndReceive(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return buildTextMessage(session, msg);
            }
        });

        printSendLongTime(start);

        try{
            ActiveMQTextMessage textMessage = (ActiveMQTextMessage) respMsg;
            return Utils.jsonToBean(textMessage.getText(), MsgDto.class);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送消息并异步接收响应数据
     * @param msg       发送端要发送的消息
     * @param onMessage 消费端的响应消息
     */
    @Override
    public void sendAndReceive(MsgDto msg, Consumer<MsgDto> onMessage) {
        CompletableFuture.runAsync(() -> {
            MsgDto respDto = null;
            try{
                respDto = sendAndReceive(msg);
            }catch (Exception e){
                MsgDto failDto = new MsgDto();
                failDto.setCause(e);
            }
            onMessage.accept(respDto);
        });
    }

    @Override
    public <T> T getTemplate() {
        return (T) jmsTemplate;
    }

    @Override
    public void destroy(){
        
    }

    private boolean isVirtualTopic(String destination){
        return destination != null && destination.startsWith("VirtualTopic.");
    }

    private Message buildTextMessage(Session session, MsgDto msg) throws JMSException {
        Map<String, String> header = msg.getHeader();
        msg.setHeader(null);//置空，避免冗余浪费空间

        if(header == null){
            header = new HashMap<>();
        }
        header.put(MQHeaders.TRACE_TRX_NO, msg.getTrxNo());
        header.put(MQHeaders.TRACE_MCH_NO, msg.getMchNo());
        header.put(MQHeaders.DESTINATION, Utils.isEmpty(msg.getTags()) ? msg.getTopic() : msg.getTopic() + ":" + msg.getTags());
        Message message = session.createTextMessage(Utils.toJson(msg));
        transferHeader(message, header);
        return message;
    }

    private void transferHeader(Message message, Map<String, String> header) throws JMSException {
        if(header != null && ! header.isEmpty()){
            for(Map.Entry<String, String> entry : header.entrySet()){
                //需要把 MQHeaders 中key进行转换
                if(MQHeaders.SCHEDULED_DELAY.equals(entry.getKey())){
                    message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, Long.valueOf(entry.getValue()));
                }else{
                    message.setStringProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void printSendLongTime(long start){
        long cost = System.currentTimeMillis() - start;
        if(cost > SEND_TOO_LONG){
            logger.warn("消息发送时间过长，耗时{}ms", cost);
        }
    }
}
