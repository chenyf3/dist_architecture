package com.xpay.starter.amq.consume;

import com.xpay.starter.amq.connection.ConnectionWrapper;
import com.xpay.starter.amq.enhance.Enhancer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.Connection;
import javax.jms.JMSException;

public class DefaultMessageListenerContainerWrapper extends DefaultMessageListenerContainer {
    private Enhancer enhancer;

    public DefaultMessageListenerContainerWrapper(Enhancer enhancer) {
        this.enhancer = enhancer;
    }

    @Override
    protected Connection createConnection() throws JMSException {
        return new ConnectionWrapper(super.createConnection(), enhancer);
    }
}
