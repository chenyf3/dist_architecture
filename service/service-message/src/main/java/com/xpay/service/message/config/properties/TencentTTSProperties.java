package com.xpay.service.message.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tencent.tts")
public class TencentTTSProperties {
    private String url = "tts.cloud.tencent.com/stream";
    private Integer connTimeout = 60;
    private String region = "ap-guangzhou";
    /**
     * 使用当前账户的appId
     */
    private String appId;
    /**
     * 当前主账户/子账户的secretId
     */
    private String secretId;
    /**
     * 当前主账户/子账户的secretKey
     */
    private String secretKey;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(Integer connTimeout) {
        this.connTimeout = connTimeout;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
