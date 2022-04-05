package com.xpay.starter.plugin.config.redis;

import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.pluginImpl.RedisLock;
import com.xpay.starter.plugin.properties.RedisLockProperties;
import com.xpay.starter.plugin.util.RedissonConnectionUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "redis.lock.enabled", havingValue = "true")
@ConditionalOnClass({Redisson.class})
@EnableConfigurationProperties(RedisLockProperties.class)
@Configuration(proxyBeanMethods = false)
public class RedisLockConfiguration {

    /**
     * redis分布式锁
     * 注：支持热更新
     * @param redisLockProperties
     * @return
     */
    @RefreshScope
    @Bean(name = "redisLock", destroyMethod = "destroy")
    public DistributedLock<RLock> redisLock(RedisLockProperties redisLockProperties) {
        RedissonClient redissonClient = RedissonConnectionUtil.createRedissonClient(redisLockProperties.getServer());
        return new RedisLock(redissonClient);
    }
}
