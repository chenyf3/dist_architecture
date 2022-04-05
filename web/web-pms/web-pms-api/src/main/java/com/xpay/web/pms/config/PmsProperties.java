package com.xpay.web.pms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PmsProperties.class)
@ConfigurationProperties(prefix = "web.pms")
public class PmsProperties {

}
