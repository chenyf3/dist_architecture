package com.xpay.starter.plugin.config.redis;

import com.xpay.starter.plugin.client.RedisClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
@ConditionalOnClass({RedisConnectionFactory.class})
@AutoConfigureAfter(org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class RedisClientConfiguration {
    private final static String CLIENT_BEAN_NAME = "redisClient";

    /**
     * redis客户端
     * 注：由于 spring-boot 自定义的 RedisConnectionFactory 并没有支持热更新，所以本客户端也就没法支持热更新了
     * @param redisConnectionFactory
     * @return
     */
    @Primary
    @Bean(name = CLIENT_BEAN_NAME, destroyMethod = "destroy")
    public RedisClient redisClient(RedisConnectionFactory redisConnectionFactory) {
        return new RedisClient(CLIENT_BEAN_NAME, redisConnectionFactory);
    }
}
