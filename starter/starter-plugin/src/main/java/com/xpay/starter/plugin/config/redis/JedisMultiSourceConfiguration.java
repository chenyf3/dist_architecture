package com.xpay.starter.plugin.config.redis;

import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.starter.plugin.properties.RedisMultiProperties;
import com.xpay.starter.plugin.properties.RedisProperties;
import com.xpay.starter.plugin.util.JedisConnectionUtil;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * jedis多数据源配置类
 * 注意：在当前版本下的spring-data-redis，如果服务端的sentinel有设置密码，JedisConnectionFactory初始化sentinel时有bug，在
 * createRedisSentinelPool 方法里面并没有把 sentinelPassword 传进去来实例化 JedisSentinelPool
 *
 */
@ConditionalOnProperty(name = "redis.multi.enabled", havingValue = "true")
@ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class})
@Configuration(proxyBeanMethods = false)
public class JedisMultiSourceConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
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

        //Spring data redis 对于sentinel设置了密码时没有配置好，有bug存在
        Map<String, RedisProperties> sources = properties.getSources();
        for(Map.Entry<String, RedisProperties> entry : sources.entrySet()){
            String beanName = entry.getKey();
            RedisProperties source = entry.getValue();
            //创建JedisConnectionFactory
            JedisConnectionFactory factory = JedisConnectionUtil.createJedisConnectionFactory(source);
            factory.afterPropertiesSet();//连接池初始化
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
