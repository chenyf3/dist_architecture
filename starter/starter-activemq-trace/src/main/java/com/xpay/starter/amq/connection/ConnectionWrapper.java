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

import javax.jms.*;

public class ConnectionWrapper implements Connection {
    private Connection connection;
    private Enhancer enhancer;

    public ConnectionWrapper(Connection connection, Enhancer enhancer) {
        this.connection = connection;
        this.enhancer = enhancer;
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return new SessionWrapper(connection.createSession(transacted, acknowledgeMode), enhancer);
    }

    @Override
    public Session createSession(int sessionMode) throws JMSException {
        return new SessionWrapper(connection.createSession(sessionMode), enhancer);
    }

    @Override
    public Session createSession() throws JMSException {
        return new SessionWrapper(connection.createSession(), enhancer);
    }

    @Override
    public String getClientID() throws JMSException {
        return connection.getClientID();
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        connection.setClientID(clientID);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return connection.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return connection.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        connection.setExceptionListener(listener);
    }

    @Override
    public void start() throws JMSException {
        connection.start();
    }

    @Override
    public void stop() throws JMSException {
        connection.stop();
        connection = null;
        enhancer = null;
    }

    @Override
    public void close() throws JMSException {
        connection.close();
        connection = null;
        enhancer = null;
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination,
                                                       String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return connection.createConnectionConsumer(destination, messageSelector, sessionPool,
                maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName,
                                                             String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return connection.createSharedConnectionConsumer(topic, subscriptionName, messageSelector,
                sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName,
                                                              String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return connection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector,
                sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic,
                                                                    String subscriptionName, String messageSelector, ServerSessionPool sessionPool,
                                                                    int maxMessages) throws JMSException {
        return connection.createSharedDurableConnectionConsumer(topic, subscriptionName,
                messageSelector, sessionPool, maxMessages);
    }
}
