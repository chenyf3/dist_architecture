package com.xpay.service.message.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tencent.sms")
public class TencentSmsProperties {
    private String url = "sms.tencentcloudapi.com";
    private Integer connTimeout = 60;
    private String region = "ap-guangzhou";//根据自身服务端所在地理位置选择不同的区域
    /**
     * 短信应用的SDK AppID，一般来说创建一个通用的应用就可以了
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
    /**
     * 默认的短信签名
     */
    private String signName;

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

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }
}
