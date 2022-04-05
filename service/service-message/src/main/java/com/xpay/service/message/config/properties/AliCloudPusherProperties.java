package com.xpay.service.message.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc 阿里云移动推送配置
 */
@ConfigurationProperties(prefix = "alicloud.pusher")
public class AliCloudPusherProperties {
    /**
     * 当前账户的 accessKey
     */
    private String accessKey;
    /**
     * 当前账户的 secretKey
     */
    private String secretKey;
    /**
     * key：app名称(详见com.xpay.facade.message.enums.AppNameEnum)，value：应用配置信息
     */
    private Map<String, App> appMap = new HashMap<>();

    /**
     * 根据自身所在区域选择不同的接入点，参考如下地址：
     * @link https://www.alibabacloud.com/help/zh/doc-detail/40654.htm?spm=a2c63.p38356.879954.8.e1d5ed69yuEV79#concept-h4v-j5k-xdb
     */
    private String regionId = "cn-guangzhou";

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

    public Map<String, App> getAppMap() {
        return appMap;
    }

    public void setAppMap(Map<String, App> appMap) {
        this.appMap = appMap;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    /**
     * 阿里云上为每个应用分配的相关配置信息
     */
    public static class App {
        private Long appKey;

        public Long getAppKey() {
            return appKey;
        }

        public void setAppKey(Long appKey) {
            this.appKey = appKey;
        }
    }
}
