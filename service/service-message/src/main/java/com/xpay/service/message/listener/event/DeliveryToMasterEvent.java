package com.xpay.service.message.listener.event;

import com.xpay.service.message.listener.model.QueueProperty;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author chenyf
 * @description 备用MQ上无消费者的消息发回主MQ事件
 * @date 2020/05/18
 */
public class DeliveryToMasterEvent extends ApplicationEvent {

    static final String TOPIC = "备用MQ的无消费者消息发回主MQ";

    public DeliveryToMasterEvent(Object source) {
        super(source);
    }

    public List<QueueProperty> getQueueList() {
        return (List<QueueProperty>) source;
    }
}
