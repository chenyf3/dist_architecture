package com.xpay.service.message.config;

import com.xpay.service.message.config.properties.TencentPusherProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯信鸽移动推送
 */
@Configuration
@EnableConfigurationProperties(TencentPusherProperties.class)
public class TencentPusherConfig {

}
