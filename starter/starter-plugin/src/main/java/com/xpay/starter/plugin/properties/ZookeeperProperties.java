package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zookeeper配置属性，用以zookeeper分布式锁等
 * @author chenyf
 */
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperProperties {
    /**
     * 连接地址{ip:port}，多个地址用逗号分割
     */
    private String urls;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * session超时时间，毫秒
     */
    private int sessionTimeoutMs = 60 * 1000;
    /**
     * connection超时时间，毫秒
     */
    private int connectionTimeoutMs = 15 * 1000;
    /**
     * 最大重试次数
     */
    private int maxRetry = 1;
    /**
     * 重试间隔
     */
    private int retryIntervalMs = 1000;
    /**
     * 是否开群Zookeeper的分布式锁
     */
    private boolean lockEnabled = false;

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(int retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public boolean getLockEnabled() {
        return lockEnabled;
    }

    public void setLockEnabled(boolean lockEnabled) {
        this.lockEnabled = lockEnabled;
    }
}
