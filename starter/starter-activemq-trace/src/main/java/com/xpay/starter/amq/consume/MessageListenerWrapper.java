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

import com.xpay.starter.amq.config.Const;
import com.xpay.starter.amq.enhance.Enhancer;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Collections;

/**
 * Tracing decorator for JMS MessageListener
 */
public class MessageListenerWrapper implements MessageListener {
    private final MessageListener messageListener;
    private final Enhancer enhancer;
    private String destination;//消费端监听者
    private String subscription;//负责消费本消息的方法名

    public MessageListenerWrapper(MessageListener messageListener, Enhancer enhancer) {
        this.messageListener = messageListener;
        this.enhancer = enhancer;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public void onMessage(Message message) {
        try {
            messageListener.onMessage(message);
            trace(message, null);
        } catch (Throwable e) {
            trace(message, e);
            throw e;//要把异常往上抛，因为可能需要消息重新投递
        }
    }

    private void trace(Message message, Throwable e) {
        if (enhancer != null) {
            enhancer.trace(message, Enhancer.Type.CONSUME, subscription, e, Collections.singletonMap(Const.CONSUME_DEST, destination));
        }
    }
}
