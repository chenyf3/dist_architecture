package com.xpay.service.message.listener.event;

import com.xpay.service.message.listener.model.QueueProperty;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author chenyf
 * @description 消息积压事件
 * @date 2020/05/18
 */
public class MessageBacklogEvent extends ApplicationEvent {

    static final String TOPIC = "ActiveMQ队列发生消息积压";

    public MessageBacklogEvent(Object source) {
        super(source);
    }

    public List<QueueProperty> getQueueList() {
        return (List<QueueProperty>) this.source;
    }
}
