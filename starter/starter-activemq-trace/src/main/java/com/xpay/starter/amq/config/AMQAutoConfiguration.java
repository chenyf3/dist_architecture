package com.xpay.starter.amq.config;

import com.xpay.starter.amq.consume.CustomizeJmsListenerConfigurer;
import com.xpay.starter.amq.consume.DefaultJmsListenerContainerFactoryWrapper;
import com.xpay.starter.amq.consume.JmsListenerEndpointRegistryWrapper;
import com.xpay.starter.amq.consume.MessagingMessageListenerAdapterWrapper;
import com.xpay.starter.amq.enhance.EnhancerImpl;
import com.xpay.starter.amq.tracer.MQTracer;
import com.xpay.starter.amq.produce.JmsTemplateWrapper;
import com.xpay.starter.amq.enhance.Enhancer;
import com.xpay.starter.amq.tracer.LogTracer;
import com.xpay.starter.amq.util.WrapperUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.ConnectionFactory;
import java.time.Duration;

/**
 * ActiveMQ的自动配置类
 * @see org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration
 * @see org.springframework.boot.autoconfigure.jms.JmsAnnotationDrivenConfiguration
 * @see org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration
 */
@ConditionalOnClass(JmsTemplate.class)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter(ActiveMQAutoConfiguration.class)
@EnableConfigurationProperties(AMQProperties.class)
@Configuration
public class AMQAutoConfiguration {
    @Value("${spring.application.name:\"\"}")
    private String appName;

    private BeanFactory beanFactory;
    private ObjectProvider<MessageConverter> messageConverter;
    private AMQProperties amqProperties;

    public AMQAutoConfiguration(BeanFactory beanFactory, ObjectProvider<MessageConverter> messageConverter,
                                AMQProperties amqProperties) {
        this.beanFactory = beanFactory;
        this.messageConverter = messageConverter;
        this.amqProperties = amqProperties;
    }

    @Bean
    public Enhancer enhancer(ConnectionFactory connectionFactory) {
        Enhancer enhancer = new EnhancerImpl();
        enhancer.setAppName(appName);
        enhancer.setClientIp(WrapperUtil.getLocalIp());

        if (amqProperties.getTraceable()) {
            if (amqProperties.getBrokerTrace()) {
                enhancer.setTracer(new MQTracer(connectionFactory));
            } else {
                enhancer.setTracer(new LogTracer());
            }
        }
        return enhancer;
    }

    @Bean
    public JmsTemplate jmsTemplate(Enhancer enhancer, ConnectionFactory connectionFactory, JmsProperties properties) {
        JmsTemplate template = new JmsTemplateWrapper(connectionFactory, enhancer);
        MessageConverter mc = messageConverter.getIfAvailable();
        if (mc != null) {
            template.setMessageConverter(mc);
        }
        template.setPubSubDomain(properties.isPubSubDomain());
        mapTemplateProperties(properties.getTemplate(), template);
        return template;
    }

    @Bean
    public MessagingMessageListenerAdapterWrapper messagingMessageListenerAdapterWrapper(Enhancer enhancer) {
        return new MessagingMessageListenerAdapterWrapper(enhancer);
    }

    @Bean
    public JmsListenerEndpointRegistryWrapper jmsListenerEndpointRegistryWrapper(
            MessagingMessageListenerAdapterWrapper messagingMessageListenerAdapterWrapper) {
        return new JmsListenerEndpointRegistryWrapper(messagingMessageListenerAdapterWrapper);
    }

    /**
     * 通过自定义的JmsListenerConfigurer来改变注册过程中的行为
     *
     * @param jmsListenerEndpointRegistryWrapper
     * @return
     */
    @Bean
    public JmsListenerConfigurer customizeJmsListenerConfigurer(JmsListenerEndpointRegistryWrapper jmsListenerEndpointRegistryWrapper) {
        return new CustomizeJmsListenerConfigurer(jmsListenerEndpointRegistryWrapper);
    }

    /**
     * 自定义 DefaultJmsListenerContainerFactory
     *
     * @param configurer
     * @param connectionFactory
     * @return
     * @see org.springframework.boot.autoconfigure.jms.JmsAnnotationDrivenConfiguration
     */
    @Bean("jmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(DefaultJmsListenerContainerFactoryConfigurer configurer,
                                                                          ConnectionFactory connectionFactory,
                                                                          Enhancer enhancer) {
        DefaultJmsListenerContainerFactory containerFactory = new DefaultJmsListenerContainerFactoryWrapper(enhancer);
        configurer.configure(containerFactory, connectionFactory);
        return containerFactory;
    }

    /**
     * 定义消息消费失败时的重投策略
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    public RedeliveryPolicy redeliveryPolicy(ConnectionFactory connectionFactory) {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        //是否在每次尝试重新发送失败后,成倍数增长这个间隔时间
        redeliveryPolicy.setUseExponentialBackOff(true);
        //首次重发时间间隔,默认为1秒
        redeliveryPolicy.setInitialRedeliveryDelay(amqProperties.getInitialRedeliveryDelay());
        //重发时间间隔
        redeliveryPolicy.setRedeliveryDelay(redeliveryPolicy.getInitialRedeliveryDelay());
        //成倍数增长间隔时间的倍数
        redeliveryPolicy.setBackOffMultiplier(2);
        //最大间隔时间
        redeliveryPolicy.setMaximumRedeliveryDelay(amqProperties.getMaxRedeliveryDelay());
        //最大重试次数
        redeliveryPolicy.setMaximumRedeliveries(amqProperties.getMaximumRedelivery());

        if (connectionFactory instanceof JmsPoolConnectionFactory) {
            ((ActiveMQConnectionFactory) ((JmsPoolConnectionFactory) connectionFactory).getConnectionFactory()).setRedeliveryPolicy(redeliveryPolicy);
        } else if (connectionFactory instanceof CachingConnectionFactory) {
            ((ActiveMQConnectionFactory) ((CachingConnectionFactory) connectionFactory).getTargetConnectionFactory()).setRedeliveryPolicy(redeliveryPolicy);
        } else if (connectionFactory instanceof ActiveMQConnectionFactory) {
            ((ActiveMQConnectionFactory) connectionFactory).setRedeliveryPolicy(redeliveryPolicy);
        } else {
            throw new RuntimeException("未支持的ConnectionFactory类型：" + connectionFactory.getClass().getName());
        }
        return redeliveryPolicy;
    }

    /**
     * create lazy proxy, to avoid dependency and config order
     * if JMS is used, and ConnectionFactory bean is not present,
     * it will throw an error on first use, so imo, we should be all good
     *
     * @param beanFactory
     * @return
     */
    private ConnectionFactory createProxy(final BeanFactory beanFactory) {
        return (ConnectionFactory) ProxyFactory.getProxy(new AbstractLazyCreationTargetSource() {
            @Override
            public synchronized Class<?> getTargetClass() {
                return ConnectionFactory.class;
            }

            @Override
            protected Object createObject() throws Exception {
                return beanFactory.getBean(ConnectionFactory.class);
            }
        });
    }

    private void mapTemplateProperties(JmsProperties.Template properties, JmsTemplate template) {
        PropertyMapper map = PropertyMapper.get();
        map.from(properties::getDefaultDestination).whenNonNull().to(template::setDefaultDestinationName);
        map.from(properties::getDeliveryDelay).whenNonNull().as(Duration::toMillis).to(template::setDeliveryDelay);
        map.from(properties::determineQosEnabled).to(template::setExplicitQosEnabled);
        map.from(properties::getDeliveryMode).whenNonNull().as(JmsProperties.DeliveryMode::getValue)
                .to(template::setDeliveryMode);
        map.from(properties::getPriority).whenNonNull().to(template::setPriority);
        map.from(properties::getTimeToLive).whenNonNull().as(Duration::toMillis).to(template::setTimeToLive);
        map.from(properties::getReceiveTimeout).whenNonNull().as(Duration::toMillis)
                .to(template::setReceiveTimeout);
    }
}
