/*
 * Copyright 2017-2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.xpay.starter.amq.consume;

import com.xpay.starter.amq.enhance.Enhancer;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.lang.reflect.Method;

public class MessagingMessageListenerAdapterWrapper extends MessagingMessageListenerAdapter {
    protected Enhancer enhancer;
    private String destination;//监听的队列名
    private String subscription;//监听消息者

    public MessagingMessageListenerAdapterWrapper(Enhancer enhancer) {
        this.enhancer = enhancer;
    }

    public void setHandlerMethod(InvocableHandlerMethod handlerMethod) {
        super.setHandlerMethod(handlerMethod);
        Method method = handlerMethod.getMethod();
        JmsListener jmsListener = method.getAnnotation(JmsListener.class);
        if (jmsListener != null) {
            this.destination = jmsListener.destination();
        }
        if (jmsListener == null || jmsListener.subscription() == null || jmsListener.subscription().trim().length() == 0) {
            this.subscription = handlerMethod.getMethod().getName();
        } else {
            this.subscription = jmsListener.subscription();
        }
    }

    @Override
    public void onMessage(final Message jmsMessage, final Session session) throws JMSException {
        MessageListenerWrapper listenerWrapper = new MessageListenerWrapper(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                onMessageInternal(message, session);
            }
        }, enhancer);
        listenerWrapper.setDestination(destination);
        listenerWrapper.setSubscription(subscription);
        listenerWrapper.onMessage(jmsMessage);
    }

    private void onMessageInternal(Message jmsMessage, Session session) {
        try {
            super.onMessage(jmsMessage, session);
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }

    protected MessagingMessageListenerAdapterWrapper newInstance() {
        return new MessagingMessageListenerAdapterWrapper(enhancer);
    }
}
