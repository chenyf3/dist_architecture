package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.plugins.MQSender;
import com.xpay.starter.plugin.pluginImpl.RMQSender;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(RocketMQTemplate.class)
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
@Configuration
public class RMQAutoConfiguration {

    @ConditionalOnBean(RocketMQTemplate.class)
    @Bean("rmqSender")
    public MQSender rmqSender(RocketMQTemplate rocketMQTemplate){
        return new RMQSender(rocketMQTemplate);
    }

}
