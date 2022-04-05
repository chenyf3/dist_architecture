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
package com.xpay.starter.amq.connection;

import com.xpay.starter.amq.enhance.Enhancer;
import com.xpay.starter.amq.consume.MessageConsumerWrapper;
import com.xpay.starter.amq.produce.MessageProducerWrapper;

import javax.jms.*;
import java.io.Serializable;

public class SessionWrapper implements Session {
    private Session session;
    private Enhancer enhancer;

    public SessionWrapper(Session session, Enhancer enhancer) {
        this.session = session;
        this.enhancer = enhancer;
    }

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        return session.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        return session.createMapMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        return session.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        return session.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        return session.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        return session.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        return session.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        return session.createTextMessage(text);
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return session.getTransacted();
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return session.getAcknowledgeMode();
    }

    @Override
    public void commit() throws JMSException {
        session.commit();
    }

    @Override
    public void rollback() throws JMSException {
        session.rollback();
    }

    @Override
    public void close() throws JMSException {
        session.close();
        session = null;
        enhancer = null;
    }

    @Override
    public void recover() throws JMSException {
        session.recover();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return session.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        session.setMessageListener(listener);
    }

    @Override
    public void run() {
        session.run();
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        return new MessageProducerWrapper(session.createProducer(destination), enhancer);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return new MessageConsumerWrapper(session.createConsumer(destination), enhancer);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector)
            throws JMSException {
        return new MessageConsumerWrapper(session.createConsumer(destination, messageSelector), enhancer);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector,
                                          boolean noLocal) throws JMSException {
        return new MessageConsumerWrapper(session.createConsumer(destination, messageSelector, noLocal),
                enhancer);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName)
            throws JMSException {
        return new MessageConsumerWrapper(session.createSharedConsumer(topic, sharedSubscriptionName),
                enhancer);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName,
                                                String messageSelector) throws JMSException {
        return new MessageConsumerWrapper(
                session.createSharedConsumer(topic, sharedSubscriptionName, messageSelector), enhancer);
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        return session.createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        return session.createTopic(topicName);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return session.createDurableSubscriber(topic, name);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector,
                                                   boolean noLocal) throws JMSException {
        return session.createDurableSubscriber(topic, name, messageSelector, noLocal);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        return session.createDurableConsumer(topic, name);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector,
                                                 boolean noLocal) throws JMSException {
        return new MessageConsumerWrapper(
                session.createDurableConsumer(topic, name, messageSelector, noLocal), enhancer);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        return new MessageConsumerWrapper(session.createSharedDurableConsumer(topic, name), enhancer);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name,
                                                       String messageSelector) throws JMSException {
        return new MessageConsumerWrapper(
                session.createSharedDurableConsumer(topic, name, messageSelector), enhancer);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        return session.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        return session.createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        return session.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        return session.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) throws JMSException {
        session.unsubscribe(name);
    }
}
