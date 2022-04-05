package com.xpay.starter.plugin.plugins;

import com.xpay.common.statics.dto.mq.MsgDto;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface MQSender {

    /**
     * 发送单个消息（同步响应）
     * @param msg
     * @return
     */
    public boolean sendOne(MsgDto msg);

    /**
     * 发送单个消息（同步响应），如果消息发送失败会调用onFail进行处理
     * @param msg
     * @param onFail    消息发送失败之后的处理
     * @return
     */
    public void sendOne(MsgDto msg, Consumer<MsgDto> onFail);

    /**
     * 发送延时消息
     * @param msg
     * @param delaySec
     * @return
     */
    public boolean sendOneDelay(MsgDto msg, int delaySec);

    /**
     * 发送顺序消息
     * @param msg       消息体
     * @param hashKey   用以计算hash的key，比如id、订单号等，具有相同hashKey的消息会发到同一个队列中
     * @param timeout   发送超时时(毫秒)
     * @return
     */
    public boolean sendOrderly(MsgDto msg, String hashKey, long timeout);

    /**
     * 发送单个字符串类型的消息（同步响应），可自定义一些消息头
     * @param destination
     * @param body
     * @param header
     * @return
     */
    public boolean sendOne(String destination, String body, Map<String, String> header);

    /**
     * 发送单个消息且不等待响应结果，优点是快速高效，缺点是有可能丢失消息
     * @param msg
     */
    public void sendOneWay(MsgDto msg);

    /**
     * 异步发送单个消息，优点是异步化可提高并发能力和发送效率，缺点是不能保障消息不丢（尤其是在应用重启时）
     * @param msg
     * @param callback  消息发送成功或失败之后的回调函数，如果不需要处理回调则设置为null即可
     */
    public void sendOneAsync(MsgDto msg, Consumer<MsgDto> callback);

    /**
     * 异步发送单个延迟消息，优点是异步化可提高并发能力和发送效率，缺点是不能保障消息不丢（尤其是在应用重启时）
     * @param msg
     * @param delaySec
     */
    public void sendOneDelayAsync(MsgDto msg, int delaySec);

    /**
     * 批量发送消息（同步响应）
     * @param destination
     * @param msgList
     * @return
     */
    public boolean sendBatch(String destination, List<? extends MsgDto> msgList);

    /**
     * 批量发送消息（同步响应）
     * @param msgList
     * @return
     */
    public boolean sendBatch(List<? extends MsgDto> msgList);

    /**
     * 发送事务消息（同步响应）
     * @param msg
     * @return
     */
    public boolean sendTrans(MsgDto msg);

    /**
     * 发送消息并同步等待响应数据
     * @param msg
     * @return  返回消费端响应的数据
     */
    public MsgDto sendAndReceive(MsgDto msg);

    /**
     * 发送消息并异步接收响应数据
     * @param msg       发送端要发送的消息
     * @param onMessage 消费端的响应消息
     * @return
     */
    public void sendAndReceive(MsgDto msg, Consumer<MsgDto> onMessage);

    /**
     * 获取发送模板，比如：JmsTemplate、RocketMQTemplate、RabbitTemplate
     * @param <T>
     * @return
     */
    public <T> T getTemplate();

    public void destroy();
}
