package com.xpay.gateway.api.config;

import com.xpay.common.api.service.MchService;
import com.xpay.common.api.params.MchInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xpay.gateway.api.config.properties.GatewayProperties;
import com.xpay.gateway.api.filters.global.*;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.gateway.api.service.ValidService;
import com.xpay.starter.plugin.plugins.RateLimiter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import com.xpay.gateway.api.filters.factory.IPValidGatewayFilterFactory;

import java.util.Set;

@EnableConfigurationProperties(GatewayProperties.class)
@Configuration
public class GatewayConfig {
    /**-------------------------------------- 基础被依赖配置项 START --------------------------------------*/
    /**
     * guava 本地缓存
     * @return
     */
    @Bean
    public Cache<String, MchInfo> localCache(GatewayProperties gatewayProperties) {
        GatewayProperties.Cache cache = gatewayProperties.getCache();
        CacheBuilder builder = CacheBuilder.newBuilder()
                .expireAfterWrite(cache.getExpireAfterWrite())
                .maximumSize(cache.getMaximumSize())
                .initialCapacity(cache.getInitialCapacity())
                .concurrencyLevel(cache.getConcurrencyLevel());
        if(cache.getExpireAfterAccess() != null){
            builder.expireAfterAccess(cache.getExpireAfterAccess());
        }
        if(cache.getRefreshAfterWrite() != null){
            builder.refreshAfterWrite(cache.getRefreshAfterWrite());
        }
        return builder.build();
    }

    @Bean
    public ValidService validService(){
        return new ValidService();
    }

    @Bean
    public RequestHelper requestHelper(MchService mchService){
        return new RequestHelper(mchService);
    }
    /**-------------------------------------- 基础被依赖配置项 END --------------------------------------*/




    /**-------------------------------------- 全局过滤器 START --------------------------------------*/
    @Bean
    public BlackListFilter blackListFilter(GatewayProperties gatewayProperties){
        return new BlackListFilter(gatewayProperties);
    }

    @Bean
    public MaintenanceFilter maintenanceFilter(GatewayProperties gatewayProperties){
        return new MaintenanceFilter(gatewayProperties);
    }

    @Bean
    public RequestReadFilter requestReadFilter(ServerCodecConfigurer codecConfigurer, GatewayProperties gatewayProperties){
        return new RequestReadFilter(codecConfigurer, gatewayProperties);
    }

    @Bean
    public RequestParamCheckFilter requestParamCheckFilter(GatewayProperties gatewayProperties){
        return new RequestParamCheckFilter(gatewayProperties);
    }

    @Bean
    public RequestIpFilter requestIpFilter(RequestHelper requestHelper, ValidService validService){
        return new RequestIpFilter(requestHelper, validService);
    }

    @Bean
    public RequestAuthFilter requestAuthFilter(RequestHelper requestHelper, ValidService validService){
        return new RequestAuthFilter(requestHelper, validService);
    }

    @Bean
    public RequestLimitFilter requestLimitFilter(RateLimiter redisLimiter, GatewayProperties gatewayProperties){
        return new RequestLimitFilter(redisLimiter, gatewayProperties);
    }

    @Bean
    public RequestModifyFilter requestModifyFilter(RequestHelper requestHelper){
        return new RequestModifyFilter(requestHelper);
    }

    @Bean
    public RequestRewritePathFilter requestRewritePathFilter(){
        return new RequestRewritePathFilter();
    }

    @Bean
    public ResponseModifyFilter responseModifyFilter(ServerCodecConfigurer codecConfigurer, Set<MessageBodyEncoder> bodyEncoders,
                                                     Set<MessageBodyDecoder> bodyDecoders, RequestHelper requestHelper){
        return new ResponseModifyFilter(codecConfigurer, bodyEncoders, bodyDecoders, requestHelper);
    }
    /**-------------------------------------- 全局过滤器 END --------------------------------------*/




    /**-------------------------------------- 局部过滤器 START --------------------------------------*/
    @Bean
    public IPValidGatewayFilterFactory ipValidGatewayFilter(){
        return new IPValidGatewayFilterFactory();
    }
    /**-------------------------------------- 局部过滤器 END --------------------------------------*/
}
