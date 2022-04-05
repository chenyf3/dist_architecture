package com.xpay.gateway.callback.config;

import com.xpay.gateway.callback.filters.global.*;
import com.xpay.gateway.callback.helper.CompanyHelper;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;

import java.util.Set;

@EnableConfigurationProperties(GatewayProperties.class)
@Configuration
public class GatewayConfig {

    @Bean
    public CompanyHelper companyHelper(ApplicationContext applicationContext){
        return new CompanyHelper(applicationContext);
    }

    /**-------------------------------------- 全局过滤器 START --------------------------------------*/
    @Bean
    public BlackListFilter blackListFilter(GatewayProperties properties){
        return new BlackListFilter(properties);
    }

    @Bean
    public MaintenanceFilter maintenanceFilter(GatewayProperties properties){
        return new MaintenanceFilter(properties);
    }

    @Bean
    public RequestReadFilter requestReadFilter(CompanyHelper companyHelper, GatewayProperties properties){
        return new RequestReadFilter(companyHelper, properties);
    }

    @Bean
    public RequestParamCheckFilter requestParamCheckFilter(GatewayProperties properties){
        return new RequestParamCheckFilter(properties);
    }

    @Bean
    public RequestAuthFilter requestAuthFilter(CompanyHelper companyHelper){
        return new RequestAuthFilter(companyHelper);
    }

    @Bean
    public RequestModifyFilter requestModifyFilter(CompanyHelper companyHelper){
        return new RequestModifyFilter(companyHelper);
    }

    @Bean
    public RequestRewritePathFilter requestRewritePathFilter(){
        return new RequestRewritePathFilter();
    }

    @Bean
    public ResponseModifyFilter responseModifyFilter(ServerCodecConfigurer codecConfigurer, Set<MessageBodyEncoder> bodyEncoders,
                                                     Set<MessageBodyDecoder> bodyDecoders, CompanyHelper companyHelper){
        return new ResponseModifyFilter(codecConfigurer, bodyEncoders, bodyDecoders, companyHelper);
    }
    /**-------------------------------------- 全局过滤器 END --------------------------------------*/



    /**-------------------------------------- 禁止访问静态资源 START --------------------------------------*/
    @Bean
    public StaticResourceForbid staticResourceForbid(ResourceProperties resourceProperties){
        resourceProperties.setAddMappings(false);
        return new StaticResourceForbid();
    }
    public class StaticResourceForbid{
    }
}
