package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Author: Cmf
 * Description:fastdfs配置文件
 */
@ConfigurationProperties(prefix = "fastdfs")
public class FastdfsProperties {
    /**
     * tracker服务列表
     */
    private String trackerServers;
    /**
     * 通过nginx方式进行文件访问的地址
     */
    private String nginxHost;
    /**
     * 连接超时时间
     */
    private int connectTimeoutInSeconds = 60;
    /**
     * IO超时时间
     */
    private int networkTimeoutInSeconds = 80;

    private String charset = "UTF-8";

    private boolean httpAntiStealToken = false;

    private String httpSecretKey = "";

    private Integer httpTrackerHttpPort = 80;

    private Pool pool = new Pool();

    public String getTrackerServers() {
        return trackerServers;
    }

    public void setTrackerServers(String trackerServers) {
        this.trackerServers = trackerServers;
    }

    public String getNginxHost() {
        return nginxHost;
    }

    public void setNginxHost(String nginxHost) {
        this.nginxHost = nginxHost;
    }

    public int getConnectTimeoutInSeconds() {
        return connectTimeoutInSeconds;
    }

    public void setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
    }

    public int getNetworkTimeoutInSeconds() {
        return networkTimeoutInSeconds;
    }

    public void setNetworkTimeoutInSeconds(int networkTimeoutInSeconds) {
        this.networkTimeoutInSeconds = networkTimeoutInSeconds;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isHttpAntiStealToken() {
        return httpAntiStealToken;
    }

    public void setHttpAntiStealToken(boolean httpAntiStealToken) {
        this.httpAntiStealToken = httpAntiStealToken;
    }

    public String getHttpSecretKey() {
        return httpSecretKey;
    }

    public void setHttpSecretKey(String httpSecretKey) {
        this.httpSecretKey = httpSecretKey;
    }

    public Integer getHttpTrackerHttpPort() {
        return httpTrackerHttpPort;
    }

    public void setHttpTrackerHttpPort(Integer httpTrackerHttpPort) {
        this.httpTrackerHttpPort = httpTrackerHttpPort;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public class Pool {
        private Boolean enabled = true;
        private Integer maxCountPerEntry = 40;//每个实例最大多少连接数，即一个 host:port 最多允许多少连接数
        private Integer maxIdleTime = 1800;//一个connection最大空闲时间(秒)，超过此时间将会被销毁
        private Integer maxWaitTimeInMs = 2000;//当连接池不足时，客户端获取连接的等待时间(毫秒)，超过此时间将会抛出异常

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxCountPerEntry() {
            return maxCountPerEntry;
        }

        public void setMaxCountPerEntry(Integer maxCountPerEntry) {
            this.maxCountPerEntry = maxCountPerEntry;
        }

        public Integer getMaxIdleTime() {
            return maxIdleTime;
        }

        public void setMaxIdleTime(Integer maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        public Integer getMaxWaitTimeInMs() {
            return maxWaitTimeInMs;
        }

        public void setMaxWaitTimeInMs(Integer maxWaitTimeInMs) {
            this.maxWaitTimeInMs = maxWaitTimeInMs;
        }
    }
}
