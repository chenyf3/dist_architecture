package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * minio对象存储配置属性，用以存储非结构化数据，如：图片、视频、音频、压缩包、pdf、excel、日志文件、容器镜像 等等
 */
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * minio服务端连接地址，如：http://10.10.12.10:9000
     */
    private String endpoint;
    /**
     * TCP/IP Port number
     */
    private Integer port = 9000;
    /**
     * 用户名
     */
    private String accessKey;
    /**
     * 密码
     */
    private String secretKey;
    /**
     * If it is true, It uses https instead of http, The default value is false
     */
    private boolean secure = false;
    /**
     * 上传文件的最大尺寸，默认1GB
     */
    private long fileSize = 1073741824;
    /**
     * 当前写集群的名称，此集群名称会拼接到文件名中
     */
    private String writeCluster = "default";
    /**
     * 当前业务组，会被用作bucket名的前缀
     */
    private String group = "default";
    /**
     * 和minio服务端连接超时时间
     */
    private Duration connectTimeout = Duration.ofMinutes(3);
    /**
     * 向minio服务端写数据的超时时间
     */
    private Duration writeTimeout = Duration.ofMinutes(3);
    /**
     * 从minio服务端读数据的超时时间
     */
    private Duration readTimeout = Duration.ofMinutes(3);

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getWriteCluster() {
        return writeCluster;
    }

    public void setWriteCluster(String writeCluster) {
        this.writeCluster = writeCluster;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
