package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * redis分布式锁配置属性
 * @author chenyf
 */
@ConfigurationProperties(prefix = "redis.lock")
public class RedisLockProperties {
    /**
     * 是否启用redis分布式锁
     */
    private Boolean enabled = Boolean.FALSE;
    private RedisProperties server = new RedisProperties();

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
}
