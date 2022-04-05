package com.xpay.service.message.listener.event;

import com.xpay.service.message.listener.model.MetaDataDto;
import org.springframework.context.ApplicationEvent;

/**
 * @author chenyf
 * @description 同步MetaData的事件
 * @date 2020/05/18
 */
public class SyncMetaDataEvent extends ApplicationEvent {

    static final String TOPIC = "ActiveMQ同步MetaData";

    public SyncMetaDataEvent(Object source) {
        super(source);
    }

    public MetaDataDto getMetaDataDto() {
        return (MetaDataDto) this.source;
    }
}
