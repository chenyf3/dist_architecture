package com.xpay.sentinel.token.server.config;

import com.xpay.sentinel.token.server.manager.RegistryManager;
import com.xpay.sentinel.token.server.manager.TokenServerManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TokenServer服务端配置
 * @author chenyf
 */
@EnableConfigurationProperties(ServerProperties.class)
@Configuration
public class ServerConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public TokenServerManager tokenServerManager(ServerProperties properties){
        return new TokenServerManager(properties);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public RegistryManager registryManager(ServerProperties properties){
        return new RegistryManager(properties.getRegistry());
    }
}
