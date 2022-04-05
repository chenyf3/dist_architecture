package com.xpay.service.message.listener.event;

import com.xpay.service.message.listener.model.ExceptionMsg;
import org.springframework.context.ApplicationEvent;

/**
 * @author chenyf
 * @description 监控连接异常事件
 * @date 2020/05/18
 */
public class MonitorExceptionEvent extends ApplicationEvent {

    static final String TOPIC = "ActiveMQ监控时发生异常";

    public MonitorExceptionEvent(Object source) {
        super(source);
    }

    public ExceptionMsg getExceptionMsg() {
        return (ExceptionMsg) this.source;
    }
}
