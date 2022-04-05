package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * redis多数据源配置属性
 * @author chenyf
 */
@ConfigurationProperties(prefix = RedisMultiProperties.PREFIX)
public class RedisMultiProperties {
    public final static String PREFIX = "redis.multi";
    /**
     * 是否启用redis多数据源
     */
    private Boolean enabled = Boolean.FALSE;
    private Map<String, RedisProperties> sources = new HashMap<>();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, RedisProperties> getSources() {
        return sources;
    }

    public void setSources(Map<String, RedisProperties> sources) {
        this.sources = sources;
    }
}
