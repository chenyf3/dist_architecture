package com.xpay.service.message.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯信鸽移动推送
 */
@ConfigurationProperties(prefix = "tencent.pusher")
public class TencentPusherProperties {
    private String url = "https://api.tpns.tencent.com/";//根据自身所在区域选择不同的接入点
    private Integer connTimeout = 10;//链接超时时间
    private Integer readTimeOut = 10;//请求超时时间
    /**
     * key：app名称(详见com.xpay.facade.message.enums.AppNameEnum)，value：腾讯云上每个应用的配置信息
     */
    private Map<String, App> appMap = new HashMap<>();

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

    public Integer getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(Integer readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public Map<String, App> getAppMap() {
        return appMap;
    }

    public void setAppMap(Map<String, App> appMap) {
        this.appMap = appMap;
    }

    /**
     * 腾讯云上为每个应用分配的密钥相关
     */
    public static class App {
        /**
         * 当前应用的accessId
         */
        private String accessId;
        /**
         * 当前应用的secretKey
         */
        private String secretKey;
        /**
         * 当前应用的accessKey
         */
        private String accessKey;

        public String getAccessId() {
            return accessId;
        }

        public void setAccessId(String accessId) {
            this.accessId = accessId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }
    }
}
