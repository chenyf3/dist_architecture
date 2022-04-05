package com.xpay.starter.plugin.config.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.starter.plugin.pluginImpl.RedisTokenLimiter;
import com.xpay.starter.plugin.plugins.RateLimiter;
import com.xpay.starter.plugin.properties.RedisLimiterProperties;
import com.xpay.starter.plugin.properties.RedisProperties;
import com.xpay.starter.plugin.util.JedisConnectionUtil;
import com.xpay.starter.plugin.util.LettuceConnectionUtil;
import com.xpay.starter.plugin.util.RedisExistUtil;
import com.xpay.starter.plugin.util.RedissonConnectionUtil;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@ConditionalOnProperty(name = "redis.limiter.enabled", havingValue = "true")
@EnableConfigurationProperties(RedisLimiterProperties.class)
@Configuration(proxyBeanMethods = false)
public class RedisLimiterConfiguration {

    /**
     * redis限流器
     * 注：支持热更新
     * @param redisLimiterProperties
     * @return
     * @throws Exception
     */
    @RefreshScope
    @Bean(name = "redisLimiter", destroyMethod = "destroy")
    public RateLimiter redisLimiter(RedisLimiterProperties redisLimiterProperties) throws Exception {
        RedisProperties properties = redisLimiterProperties.getServer();
        RedisConnectionFactory factory;
        if (RedisExistUtil.isJedisExist()) {
            JedisConnectionFactory jedisFactory = JedisConnectionUtil.createJedisConnectionFactory(properties);
            jedisFactory.afterPropertiesSet();
            factory = jedisFactory;
        } else if (RedisExistUtil.isLettuceExist()) {
            LettuceConnectionFactory lettuceFactory = LettuceConnectionUtil.createLettuceConnectionFactory(properties, null);
            lettuceFactory.afterPropertiesSet();
            factory = lettuceFactory;
        } else if(RedisExistUtil.isRedissonExist()) {
            RedissonConnectionFactory redissonFactory = RedissonConnectionUtil.createRedissonConnectionFactory(properties);
            redissonFactory.afterPropertiesSet();
            factory = redissonFactory;
        } else {
            throw new IllegalArgumentException("请引入相关的redis客户端依赖，如：jedis/lettuce/redisson");
        }

        RedisClient client = new RedisClient("limiterRedisClient", factory);
        RedisLimiterProperties.Cache cache = redisLimiterProperties.getCache();
        Cache<String, String> shaCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cache.getDuration())
                .maximumSize(cache.getMaximumSize())
                .initialCapacity(cache.getInitialCapacity())
                .concurrencyLevel(cache.getConcurrencyLevel())
                .build();
        return new RedisTokenLimiter(client, shaCache);
    }
}
