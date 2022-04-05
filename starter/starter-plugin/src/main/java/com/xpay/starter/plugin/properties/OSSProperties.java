package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oss")
public class OSSProperties {
    private String host = "oss-cn-guangzhou.aliyuncs.com";//根据自身所在区域来配置
    /**
     * 是否开启OSS对象存储
     */
    private Boolean enable = Boolean.FALSE;
    /**
     * 当前账户的 accessKey
     */
    private String accessKey;
    /**
     * 当前账户的 secretKey
     */
    private String secretKey;
    /**
     * 默认的bucket名
     */
    private String defaultBucket;

    // 设置OSSClient允许打开的最大HTTP连接数。
    private Integer maxConnections = 200;
    // 设置Socket层传输数据的超时时间。
    private Integer socketTimeout = 10000;
    // 设置建立连接的超时时间。
    private Integer connectionTimeout = 5000;
    // 设置从连接池中获取连接的超时时间（单位：毫秒）。
    private Integer connectionRequestTimeout = 1000;
    // 设置连接空闲超时时间。超时则关闭连接。
    private Integer idleConnectionTime = 10000;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }

    public void setDefaultBucket(String defaultBucket) {
        this.defaultBucket = defaultBucket;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public Integer getIdleConnectionTime() {
        return idleConnectionTime;
    }

    public void setIdleConnectionTime(Integer idleConnectionTime) {
        this.idleConnectionTime = idleConnectionTime;
    }
}
