package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * redis限流器的配置属性
 * @author chenyf
 */
@ConfigurationProperties(prefix = "redis.limiter")
public class RedisLimiterProperties {
    /**
     * 是否启用redis限流器
     */
    private Boolean enabled = Boolean.FALSE;
    private RedisProperties server = new RedisProperties();
    private Cache cache = new Cache();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public RedisProperties getServer() {
        return server;
    }

    public void setServer(RedisProperties server) {
        this.server = server;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * 本地缓存的配置项
     */
    public static class Cache {
        /**
         * 过期时间
         */
        private Duration duration = Duration.ofSeconds(60);

        /**
         * 最大长度
         */
        private Integer maximumSize = 10000;

        /**
         * 初始容量
         */
        private Integer initialCapacity = 50;

        private Integer concurrencyLevel = 10;

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public Integer getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(Integer maximumSize) {
            this.maximumSize = maximumSize;
        }

        public Integer getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(Integer initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public Integer getConcurrencyLevel() {
            return concurrencyLevel;
        }

        public void setConcurrencyLevel(Integer concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
        }
    }
}
