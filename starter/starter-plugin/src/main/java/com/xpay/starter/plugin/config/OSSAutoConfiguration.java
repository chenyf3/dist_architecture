package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.client.OSSClient;
import com.xpay.starter.plugin.client.ObjectStorage;
import com.xpay.starter.plugin.properties.OSSProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS对象存储配置
 */
@EnableConfigurationProperties(OSSProperties.class)
@ConditionalOnClass(com.aliyun.oss.OSSClient.class)
@Configuration
public class OSSAutoConfiguration {

    @ConditionalOnProperty(name = "oss.enable", havingValue = "true")
    @Bean(name = "ossStorage", destroyMethod = "destroy")
    public ObjectStorage ossStorage(OSSProperties properties){
        return new OSSClient(properties);
    }
}
