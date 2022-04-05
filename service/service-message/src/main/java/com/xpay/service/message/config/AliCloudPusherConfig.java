package com.xpay.service.message.config;

import com.xpay.service.message.config.properties.AliCloudPusherProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliCloudPusherProperties.class)
public class AliCloudPusherConfig {

}
