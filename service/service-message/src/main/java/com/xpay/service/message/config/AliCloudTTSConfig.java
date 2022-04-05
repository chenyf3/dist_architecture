package com.xpay.service.message.config;

import com.xpay.service.message.config.properties.AliCloudTTSProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliCloudTTSProperties.class)
public class AliCloudTTSConfig {

}
