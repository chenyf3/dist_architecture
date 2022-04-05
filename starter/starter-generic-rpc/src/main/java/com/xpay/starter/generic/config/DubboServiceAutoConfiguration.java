package com.xpay.starter.generic.config;

import com.xpay.starter.generic.invoker.DubboServiceInvoker;
import com.xpay.starter.generic.hepler.GlobalLockHelper;
import com.xpay.starter.generic.service.BaseService;
import org.apache.dubbo.spring.boot.autoconfigure.DubboAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@AutoConfigureAfter(DubboAutoConfiguration.class)
public class DubboServiceAutoConfiguration {
    @Autowired
    ConfigurableEnvironment environment;

    @Bean(destroyMethod = "destroy")
    public DubboServiceInvoker dubboServiceInvoker() {
        String appName = environment.getProperty("dubbo.application.name");
        String address = environment.getProperty("dubbo.registry.address");
        Integer port = environment.getProperty("dubbo.registry.port", Integer.class, null);
        String username = environment.getProperty("dubbo.registry.username");
        String password = environment.getProperty("dubbo.registry.password");
        return new DubboServiceInvoker(appName, address, port, username, password);
    }
    
    @Bean
    public BaseService baseService(){
        return new BaseService(dubboServiceInvoker());
    }

    @ConditionalOnProperty(value = "xpay.generic.global-lock.enabled", havingValue = "true")
    @Bean(destroyMethod = "destroy")
    public GlobalLockHelper globalLockHelper(BaseService baseService){
        return new GlobalLockHelper(baseService);
    }
}
