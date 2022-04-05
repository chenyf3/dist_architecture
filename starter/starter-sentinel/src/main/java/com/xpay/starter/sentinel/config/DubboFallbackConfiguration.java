package com.xpay.starter.sentinel.config;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.xpay.starter.sentinel.dubbo.DubboConsumerFallback;
import com.xpay.starter.sentinel.dubbo.DubboProviderFallback;
import org.apache.dubbo.rpc.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * 配置dubbo处理sentinel抛出异常的全局处理器
 * @author chenyf
 */
@ConditionalOnClass({DubboAdapterGlobalConfig.class, Result.class})
@Configuration
public class DubboFallbackConfiguration {

    public DubboFallbackConfiguration(){
        DubboAdapterGlobalConfig.setProviderFallback(new DubboProviderFallback());
        DubboAdapterGlobalConfig.setConsumerFallback(new DubboConsumerFallback());
    }
}
