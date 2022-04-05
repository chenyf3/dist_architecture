package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.pluginImpl.AMQSender;
import com.xpay.starter.plugin.plugins.MQSender;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@ConditionalOnClass(JmsTemplate.class)
@AutoConfigureAfter(ActiveMQAutoConfiguration.class)
@Configuration
public class AMQAutoConfiguration {

    @Bean("amqSender")
    public MQSender amqSender(JmsTemplate jmsTemplate){
        return new AMQSender(jmsTemplate);
    }

}
