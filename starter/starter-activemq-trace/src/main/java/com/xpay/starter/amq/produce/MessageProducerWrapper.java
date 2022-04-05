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
package com.xpay.starter.amq.produce;

import com.xpay.starter.amq.enhance.Enhancer;

import javax.jms.*;

/**
 * Tracing decorator for JMS MessageProducer
 */
public class MessageProducerWrapper implements MessageProducer {
    private MessageProducer messageProducer;//如果有开启缓存功能，此属性的实例对象为：org.messaginghub.pooled.jms.JmsPoolMessageProducer
    private Enhancer enhancer;

    public MessageProducerWrapper(MessageProducer messageProducer, Enhancer enhancer) {
        this.messageProducer = messageProducer;
        this.enhancer = enhancer;
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return messageProducer.getDisableMessageID();
    }

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        messageProducer.setDisableMessageID(value);
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return messageProducer.getDisableMessageTimestamp();
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        messageProducer.setDisableMessageTimestamp(value);
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return messageProducer.getDeliveryMode();
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        messageProducer.setDeliveryMode(deliveryMode);
    }

    @Override
    public int getPriority() throws JMSException {
        return messageProducer.getPriority();
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        messageProducer.setPriority(defaultPriority);
    }

    @Override
    public long getTimeToLive() throws JMSException {
        return messageProducer.getTimeToLive();
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        messageProducer.setTimeToLive(timeToLive);
    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        return messageProducer.getDeliveryDelay();
    }

    @Override
    public void setDeliveryDelay(long deliveryDelay) throws JMSException {
        messageProducer.setDeliveryDelay(deliveryDelay);
    }

    @Override
    public Destination getDestination() throws JMSException {
        return messageProducer.getDestination();
    }

    @Override
    public void close() throws JMSException {
        messageProducer.close();
        messageProducer = null;
        enhancer = null;
    }

    @Override
    public void send(Message message) throws JMSException {
        doSend(null, message, getDeliveryMode(), getPriority(), getTimeToLive(), null);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive)
            throws JMSException {
        doSend(null, message, deliveryMode, priority, timeToLive, null);
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        doSend(destination, message, getDeliveryMode(), getPriority(), getTimeToLive(), null);
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority,
                     long timeToLive) throws JMSException {
        doSend(destination, message, deliveryMode, priority, timeToLive, null);
    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {
        doSend(null, message, getDeliveryMode(), getPriority(), getTimeToLive(), completionListener);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive,
                     CompletionListener completionListener) throws JMSException {
        doSend(null, message, deliveryMode, priority, timeToLive, completionListener);
    }

    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener)
            throws JMSException {
        doSend(destination, message, getDeliveryMode(), getPriority(), getTimeToLive(), completionListener);
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority,
                     long timeToLive, CompletionListener completionListener) throws JMSException {
        doSend(destination, message, deliveryMode, priority, timeToLive, completionListener);
    }

    private void doSend(Destination destination, Message message, int deliveryMode, int priority, long timeToLive,
                        CompletionListener completionListener) throws JMSException {

        try {
            if (destination == null) {
                if (completionListener == null) {
                    messageProducer.send(message, deliveryMode, priority, timeToLive);
                } else {
                    messageProducer.send(message, deliveryMode, priority, timeToLive, completionListener);
                }
            } else if (completionListener == null) {
                messageProducer.send(destination, message, deliveryMode, priority, timeToLive);
            } else {
                messageProducer.send(destination, message, deliveryMode, priority, timeToLive, completionListener);
            }
            trace(message, null);
        } catch (Throwable e) {
            trace(message, e);
            throw e;
        }
    }

    private void trace(Message message, Throwable e) {
        if (enhancer != null) {
            enhancer.trace(message, Enhancer.Type.PRODUCE, "", e, null);
        }
    }
}
