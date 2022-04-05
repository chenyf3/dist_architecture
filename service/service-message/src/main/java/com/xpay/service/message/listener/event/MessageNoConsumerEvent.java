package com.xpay.service.message.listener.event;

import com.xpay.service.message.listener.model.QueueProperty;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author chenyf
 * @description 消息无消费者事件
 * @date 2020/05/18
 */
public class MessageNoConsumerEvent extends ApplicationEvent {

    static final String TOPIC = "ActiveMQ队列无消费者";

    public MessageNoConsumerEvent(Object source) {
        super(source);
    }

    public List<QueueProperty> getQueueList() {
        return (List<QueueProperty>) source;
    }
}
