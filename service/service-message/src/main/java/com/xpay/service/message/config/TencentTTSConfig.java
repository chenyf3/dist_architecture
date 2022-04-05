package com.xpay.service.message.config;

import com.xpay.service.message.config.properties.TencentTTSProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TencentTTSProperties.class)
public class TencentTTSConfig {

}
