package com.xpay.service.message.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alicloud.tts")
public class AliCloudTTSProperties {
    /**
     * 当前账户的 accessKey
     */
    private String accessKey;
    /**
     * 当前账户的 secretKey
     */
    private String secretKey;
    /**
     * 当前项目的appKey，一般来说创建一个通用的项目就可以了
     */
    private String appKey;
    /**
     * 是否延迟初始化，如果为true，则只有在实际使用的时候才会去初始化客户端实例
     */
    private Boolean initDelay = Boolean.TRUE;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
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

    public Boolean getInitDelay() {
        return initDelay;
    }

    public void setInitDelay(Boolean initDelay) {
        this.initDelay = initDelay;
    }
}
