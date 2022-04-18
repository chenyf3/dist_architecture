package com.xpay.starter.plugin.pluginImpl;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.starter.plugin.consts.MQHeaders;
import com.xpay.starter.plugin.plugins.MQSender;
import com.xpay.starter.plugin.util.Utils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQLocalRequestCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * RocketMQ消息发送器
 * @author chenyf
 */
public class RMQSender implements MQSender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RocketMQTemplate rocketMQTemplate;

    public RMQSender(RocketMQTemplate rocketMQTemplate){
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送单个消息
     * @param msg
     * @return
     */
    public boolean sendOne(MsgDto msg) {
        Message message = buildMessage(msg, false);

        SendResult sendResult;
        Integer delayLevel = getDelayLevel(message);
        if(delayLevel == null){
            sendResult = rocketMQTemplate.syncSend(getDestination(msg.getTopic(), msg.getTags()), message);
        }else{
            sendResult = rocketMQTemplate.syncSend(getDestination(msg.getTopic(), msg.getTags()), message, rocketMQTemplate.getProducer().getSendMsgTimeout(), delayLevel);
        }
        return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
    }

    /**
     * 发送单个消息
     * @param msg
     * @param onFail    消息发送失败之后的处理
     * @return
     */
    public void sendOne(MsgDto msg, Consumer<MsgDto> onFail) {
        Message message = buildMessage(msg, false);

        SendResult sendResult = rocketMQTemplate.syncSend(getDestination(msg.getTopic(), msg.getTags()), message);
        if(! SendStatus.SEND_OK.equals(sendResult.getSendStatus()) && onFail != null){
            msg.setCause(new RuntimeException("Send Fail, SendStatus: " + sendResult.getSendStatus().name()));
            onFail.accept(msg);
        }
    }

    public boolean sendOne(String destination, String body, Map<String, String> header){
        MessageBuilder builder = MessageBuilder.withPayload(body);
        transferHeader(builder, header);
        Message msg = builder.build();
        SendResult sendResult = rocketMQTemplate.syncSend(destination, msg);
        return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
    }

    @Override
    public boolean sendOneDelay(MsgDto msg, int delaySec) {
        if(msg.getHeader() == null){
            msg.setHeader(new HashMap<>());
        }
        msg.getHeader().put(MQHeaders.SCHEDULED_DELAY, String.valueOf(delaySec));
        return sendOne(msg);
    }

    /**
     * 发送顺序消息，如果一个topic配置成只有一个queue，则可以实现全局有序，但是性能较差，而如果一个topic下配置了多个queue，
     * 就需要确保确保具有相同hashKey的消息发到相同的queue即可，能做到局部有序，所以，此功能要求不能更改topic下的queue数量。
     * @param msg       消息体
     * @param hashKey   用以计算hash的key，比如id、订单号等，具有相同hashKey的消息会发到同一个队列中
     * @param timeout   发送超时时(毫秒)
     * @return
     */
    @Override
    public boolean sendOrderly(MsgDto msg, String hashKey, long timeout){
        Message message = buildMessage(msg, false);
        String destination = getDestination(msg.getTopic(), msg.getTags());
        SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination, message, hashKey, timeout);
        return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
    }

    /**
     * 发送单个消息且不等待响应结果，优点是快速高效，缺点是有可能丢失消息
     * @param msg
     */
    public void sendOneWay(MsgDto msg) {
        Message message = buildMessage(msg, false);
        rocketMQTemplate.sendOneWay(getDestination(msg.getTopic(), msg.getTags()), message);
    }

    /**
     * 异步发送单个消息，优点是异步化可提高并发能力和发送效率，缺点是不能保障消息不丢（尤其是在应用重启时）
     * @param msg
     * @param callback  消息发送成功或失败之后的回调函数，如果不需要处理回调则设置为null即可
     */
    public void sendOneAsync(MsgDto msg, Consumer<MsgDto> callback) {
        Message message = buildMessage(msg, false);
        rocketMQTemplate.asyncSend(getDestination(msg.getTopic(), msg.getTags()), message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                if(callback != null){
                    if(SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                        callback.accept(msg);
                    }else{
                        onException(new Throwable(sendResult.getSendStatus().name()));
                    }
                }
            }

            @Override
            public void onException(Throwable e) {
                if(callback != null){
                    msg.setCause(e);
                    callback.accept(msg);
                }
            }
        });
    }

    @Override
    public void sendOneDelayAsync(MsgDto msg, int delaySec) {
        if(msg.getHeader() == null){
            msg.setHeader(new HashMap<>());
        }
        msg.getHeader().put(MQHeaders.SCHEDULED_DELAY, String.valueOf(delaySec));
        sendOneAsync(msg, null);
    }

    /**
     * 发送批量消息，适合同一个业务事件有多个业务系统需要做不同业务处理的时候使用
     * 注意：4.5.2版本下Broker端使用DledgerCommitLog模式时还不支持批量消息，会报 [CODE: 13 MESSAGE_ILLEGAL] 的异常，在常规的Master-Slave下可以
     * @param destination   目的地，如果只有topic，则只传topic名称即可，如果还有tags，则拼接成 topic:tags 的形式
     * @param msgList
     * @return
     */
    public boolean sendBatch(String destination, List<? extends MsgDto> msgList){
        try {
            long now = 0;
            boolean isDebugEnabled = logger.isDebugEnabled();
            if(isDebugEnabled){
                now = System.currentTimeMillis();
            }

            List<Message> springMsgList = new ArrayList<>(msgList.size());
            for(MsgDto msg : msgList){
                Message message = buildMessage(msg, false);
                springMsgList.add(message);
            }
            SendResult sendResult = rocketMQTemplate.syncSend(destination, springMsgList, 3000L);
            if(isDebugEnabled){
                logger.debug("sendBatch message cost: {} ms, msgId:{}", (System.currentTimeMillis()-now), sendResult.getMsgId());
            }
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Throwable e) {
            logger.error("sendBatch failed. destination:{}, msgList:{} ", destination, Utils.toJson(msgList));
            throw new RuntimeException("批量消息发送异常", e);
        }
    }

    /**
     * 发送批量消息，一批次的消息只能发送给同一个topic，但tags可以不一样
     * @see #sendBatch(String, List)
     * @param msgList
     * @return
     */
    public boolean sendBatch(List<? extends MsgDto> msgList){
        try {
            long now = 0;
            boolean isDebugEnabled = logger.isDebugEnabled();
            if(isDebugEnabled){
                now = System.currentTimeMillis();
            }

            List<org.apache.rocketmq.common.message.Message> rmsgList = new ArrayList<>(msgList.size());
            for(MsgDto msg : msgList){
                Message<?> message = buildMessage(msg, true);
                String destination = getDestination(msg.getTopic(), msg.getTags());
                org.apache.rocketmq.common.message.Message rocketMsg = RocketMQUtil.convertToRocketMessage(
                        rocketMQTemplate.getMessageConverter(), "utf-8", destination, message);
                rmsgList.add(rocketMsg);
            }

            SendResult sendResult = rocketMQTemplate.getProducer().send(rmsgList);
            if(isDebugEnabled){
                logger.debug("sendBatch message cost: {} ms, msgId:{}", (System.currentTimeMillis()-now), sendResult.getMsgId());
            }
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Throwable e) {
            logger.error("sendBatch failed. msgList:{} ", Utils.toJson(msgList));
            throw new RuntimeException("批量消息发送异常", e);
        }
    }

    /**
     * 发送事务消息
     * @param msg
     * @return
     */
    public boolean sendTrans(MsgDto msg) {
        Message message = buildMessage(msg, false);
        SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(getDestination(msg.getTopic(), msg.getTags()), message, null);
        return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
    }

    /**
     * 送消息并同步等待响应数据
     * @param msg
     * @return
     */
    @Override
    public MsgDto sendAndReceive(MsgDto msg) {
        Message message = buildMessage(msg, false);
        return rocketMQTemplate.sendAndReceive(getDestination(msg.getTopic(), msg.getTags()), message, MsgDto.class);
    }

    /**
     * 发送消息并异步接收响应数据
     * @param msg       发送端要发送的消息
     * @param onMessage 消费端的响应消息
     */
    @Override
    public void sendAndReceive(MsgDto msg, Consumer<MsgDto> onMessage) {
        Message message = buildMessage(msg, false);

        rocketMQTemplate.sendAndReceive(getDestination(msg.getTopic(), msg.getTags()), message, new RocketMQLocalRequestCallback<MsgDto>() {
            @Override
            public void onSuccess(MsgDto message) {
                onMessage.accept(message);
            }

            @Override
            public void onException(Throwable e) {
                MsgDto failDto = new MsgDto();
                failDto.setCause(e);
                onMessage.accept(failDto);
            }
        });
    }

    @Override
    public <T> T getTemplate() {
        return (T) rocketMQTemplate;
    }

    @Override
    public void destroy(){
        if(rocketMQTemplate != null){
            rocketMQTemplate.destroy();
        }
    }

    public static String getDestination(String topic , String tags){
        return Utils.isEmpty(tags) ? topic : topic + MsgDto.SEPARATOR + tags;
    }

    private Message buildMessage(MsgDto msg, boolean msgToByte){
        Map<String, String> header = msg.getHeader();
        msg.setHeader(null);//置空，避免冗余浪费空间

        if(header == null){
            header = new HashMap<>();
        }
        header.put(MQHeaders.TRACE_TRX_NO, msg.getTrxNo());
        MessageBuilder builder = MessageBuilder.withPayload(msgToByte ? Utils.toJson(msg).getBytes(StandardCharsets.UTF_8) : msg);
        transferHeader(builder, header);
        return builder.build();
    }

    //把消息头设置到MQ消息头里面去
    private void transferHeader(MessageBuilder builder, Map<String, String> header){
        if(header != null && !header.isEmpty()){
            for(Map.Entry<String, String> entry : header.entrySet()){
                if(Utils.isEmpty(entry.getKey()) || Utils.isEmpty(entry.getValue())){
                    continue;
                }
                //需要把MQHeaders中key进行转换
                if(MQHeaders.TRACE_TRX_NO.equals(entry.getKey())){
                    builder.setHeader(MessageConst.PROPERTY_KEYS, entry.getValue());
                }else if(MQHeaders.SCHEDULED_DELAY.equals(entry.getKey())){
                    builder.setHeader(MessageConst.PROPERTY_DELAY_TIME_LEVEL, convertToDelayLevel(entry.getValue()));
                }else{
                    builder.setHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * RocketMQ不支持精确地延时，只支持特定级别的延迟，延迟级别有：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * 所以，此处的延时级别是向右取整的方式，比如定时秒数是25，则会取30秒的级别，定时秒数是80、110，都会取120秒的级别
     * @param secondStr
     * @return
     */
    private int convertToDelayLevel(String secondStr){
        int seconds = Integer.valueOf(secondStr);
        if(seconds <= 1){
            return 1;
        }else if(seconds <= 5){
            return 2;
        }else if(seconds <= 10){
            return 3;
        }else if(seconds <= 30){
            return 4;
        }else if(seconds <= 1 * 60){
            return 5;
        }else if(seconds <= 2 * 60){
            return 6;
        }else if(seconds <= 3 * 60){
            return 7;
        }else if(seconds <= 4 * 60){
            return 8;
        }else if(seconds <= 5 * 60){
            return 9;
        }else if(seconds <= 6 * 60){
            return 10;
        }else if(seconds <= 7 * 60){
            return 11;
        }else if(seconds <= 8 * 60){
            return 12;
        }else if(seconds <= 9 * 60){
            return 13;
        }else if(seconds <= 10 * 60){
            return 14;
        }else if(seconds <= 20 * 60){
            return 15;
        }else if(seconds <= 30 * 60){
            return 16;
        }else if(seconds <= 1 * 60 * 60){
            return 17;
        }else{
            return 18;
        }
    }

    private Integer getDelayLevel(Message message){
        Object obj = message.getHeaders().get(MessageConst.PROPERTY_DELAY_TIME_LEVEL);
        return obj != null ? (Integer)obj : null;
    }
}
