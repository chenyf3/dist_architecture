package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.consts.DeadLetter;
import com.xpay.starter.plugin.pluginImpl.RAQSender;
import com.xpay.starter.plugin.plugins.MQSender;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(RabbitTemplate.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@Configuration
public class RAQAutoConfiguration {
    @ConditionalOnBean(RabbitTemplate.class)
    @Bean("raqSender")
    public MQSender raqSender(RabbitTemplate rabbitTemplate){
        return new RAQSender(rabbitTemplate);
    }

    /**
     * 死信交换机，使用Fanout交换器，这样就可以不用设置RoutingKey，但要注意别绑定多个Queue
     * @return
     */
    @Bean("dlx")
    public FanoutExchange dlx(){
        return new FanoutExchange(DeadLetter.DLX, true, false);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("dlq")
    public Queue dlq(){
        return new Queue(DeadLetter.DLQ, true);
    }

    /**
     * 死信交换机和死信队列的绑定
     * @return
     */
    @Bean("dlqBinding")
    public Binding dlqBinding(){
        return BindingBuilder.bind(dlq()).to(dlx());
    }
}
