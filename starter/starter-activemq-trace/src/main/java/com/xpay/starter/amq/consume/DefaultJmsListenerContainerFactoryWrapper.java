package com.xpay.starter.amq.consume;

import com.xpay.starter.amq.enhance.Enhancer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class DefaultJmsListenerContainerFactoryWrapper extends DefaultJmsListenerContainerFactory {
    private Enhancer enhancer;

    public DefaultJmsListenerContainerFactoryWrapper(Enhancer enhancer) {
        this.enhancer = enhancer;
    }

    @Override
    protected DefaultMessageListenerContainer createContainerInstance() {
        return new DefaultMessageListenerContainerWrapper(enhancer);
    }
}
