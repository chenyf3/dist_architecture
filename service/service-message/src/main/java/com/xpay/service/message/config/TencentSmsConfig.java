package com.xpay.service.message.config;

import com.xpay.service.message.config.properties.TencentSmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TencentSmsProperties.class)
public class TencentSmsConfig {

}
