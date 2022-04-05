package com.xpay.starter.plugin.config.redis;

import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.starter.plugin.properties.RedisMultiProperties;
import com.xpay.starter.plugin.properties.RedisProperties;
import com.xpay.starter.plugin.util.RedissonConnectionUtil;
import org.redisson.Redisson;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;

import java.util.Map;

/**
 * redisson多数据源配置类
 */
@ConditionalOnProperty(name = "redis.multi.enabled", havingValue = "true")
@ConditionalOnClass({Redisson.class, RedisOperations.class, RedissonConnectionFactory.class})
@Configuration(proxyBeanMethods = false)
public class RedissonMultiSourceConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //从环境对象取得配置属性
        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
        final Binder binder = Binder.get(env);
        BindResult<RedisMultiProperties> bind = binder.bind(RedisMultiProperties.PREFIX, RedisMultiProperties.class);
        RedisMultiProperties properties = bind.get();
        if(properties == null || !properties.getEnabled()){
            return;
        }

        Map<String, RedisProperties> sources = properties.getSources();
        for(Map.Entry<String, RedisProperties> entry : sources.entrySet()){
            String beanName = entry.getKey();
            RedisProperties source = entry.getValue();
            //创建 RedissonConnectionFactory
            RedissonConnectionFactory factory = RedissonConnectionUtil.createRedissonConnectionFactory(source);
            try {
                factory.afterPropertiesSet();//连接池初始化
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //创建bean信息
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisClient.class);
            beanDefinitionBuilder.addConstructorArgValue(beanName);
            beanDefinitionBuilder.addConstructorArgValue(factory);
            beanDefinitionBuilder.setDestroyMethodName("destroy");
            //动态注册bean
            registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
