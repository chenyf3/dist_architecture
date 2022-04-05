package com.xpay.starter.sentinel.config;

import com.xpay.starter.sentinel.manager.RuleManager;
import com.xpay.starter.sentinel.manager.TokenClientManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置sentinel客户端，包括：流控规则(可选)、集群流控tokenServer
 * @author chenyf
 */
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientProperties.class)
@Configuration
public class ClientAutoConfiguration {

    @ConditionalOnProperty(name = "spring.cloud.sentinel.client.enabled", havingValue = "true", matchIfMissing = true)
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public RuleManager ruleManager(ClientProperties properties){
        return new RuleManager(properties.getAppName(), properties.getRuleServer());
    }

    @ConditionalOnProperty(name = "spring.cloud.sentinel.client.enabled", havingValue = "true", matchIfMissing = true)
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public TokenClientManager tokenClientManager(ClientProperties properties){
        return new TokenClientManager(properties);
    }
}
