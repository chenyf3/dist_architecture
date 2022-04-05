package com.xpay.sentinel.token.server.config.inet;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "sentinel.util.enabled", matchIfMissing = true)
@AutoConfigureOrder(0)
@EnableConfigurationProperties
public class UtilAutoConfiguration {

    @Bean
    public InetUtils.InetUtilsProperties inetUtilsProperties() {
        return new InetUtils.InetUtilsProperties();
    }
}
