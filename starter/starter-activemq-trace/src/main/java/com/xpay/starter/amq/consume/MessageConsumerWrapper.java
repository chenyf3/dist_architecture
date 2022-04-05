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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

/**
 * Tracing decorator for JMS MessageConsumer
 */
public class MessageConsumerWrapper implements MessageConsumer {
    private MessageConsumer messageConsumer;
    private Enhancer enhancer;

    public MessageConsumerWrapper(MessageConsumer messageConsumer, Enhancer enhancer) {
        this.messageConsumer = messageConsumer;
        this.enhancer = enhancer;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return messageConsumer.getMessageSelector();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return messageConsumer.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        if (listener instanceof MessageConsumerWrapper) {
            messageConsumer.setMessageListener(listener);
        } else {
            messageConsumer.setMessageListener(new MessageListenerWrapper(listener, enhancer));
        }
    }

    @Override
    public Message receive() throws JMSException {
        Message message = messageConsumer.receive();
        return message;
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        Message message = messageConsumer.receive(timeout);
        return message;
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        Message message = messageConsumer.receiveNoWait();
        return message;
    }

    @Override
    public void close() throws JMSException {
        messageConsumer.close();
        messageConsumer = null;
    }
}
