package com.xpay.starter.monitor.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus的配置类
 */
@Configuration
public class PrometheusAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String appName) {
        return (registry) -> registry.config().commonTags("application", appName);
    }
}
