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

import com.xpay.starter.amq.connection.ConnectionWrapper;
import com.xpay.starter.amq.enhance.Enhancer;
import com.xpay.starter.amq.util.WrapperUtil;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;

// Decorator for Spring JmsTemplate
public class JmsTemplateWrapper extends JmsTemplate {
    private final Enhancer enhancer;

    public JmsTemplateWrapper(Enhancer enhancer) {
        super();
        this.enhancer = enhancer;
    }

    public JmsTemplateWrapper(ConnectionFactory connectionFactory, Enhancer enhancer) {
        super(connectionFactory);
        this.enhancer = enhancer;
    }

    /**
     * 重写此方法，为消息体增加头信息，说明：
     * 1、在JmsTemplate中的各种 execute(...) 方法，这是属于用户自定义消息的行为，此处无法得到消息体，故不在消息轨迹追踪等处理范围内
     *
     * @param producer
     * @param message
     * @throws JMSException
     */
    @Override
    protected void doSend(MessageProducer producer, Message message) throws JMSException {
        if (enhancer.getTracer() != null) {
            enhancer.getTracer().setTraceId(message, WrapperUtil.getUniqueId());
        }
        super.doSend(producer, message);
    }

    @Override
    protected Connection createConnection() throws JMSException {
        Connection connection = super.createConnection();
        return new ConnectionWrapper(connection, enhancer);
    }
}
