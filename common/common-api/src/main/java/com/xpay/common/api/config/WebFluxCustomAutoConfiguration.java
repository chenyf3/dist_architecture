package com.xpay.common.api.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;
import com.xpay.common.api.webflux.GlobalExceptionHandler;
import com.xpay.common.api.webflux.GlobalErrorAttributes;

import java.util.Collections;
import java.util.List;

/**
 * @description 覆盖默认的异常处理
 * @author: chenyf
 * @Date: 2019-02-23
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@AutoConfigureBefore(ErrorWebFluxAutoConfiguration.class)
public class WebFluxCustomAutoConfiguration {
    private final ServerProperties serverProperties;

    private final ApplicationContext applicationContext;

    private final ResourceProperties resourceProperties;

    private final List<ViewResolver> viewResolvers;

    private final ServerCodecConfigurer serverCodecConfigurer;

    public WebFluxCustomAutoConfiguration(ServerProperties serverProperties,
                                          ResourceProperties resourceProperties,
                                          ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                          ServerCodecConfigurer serverCodecConfigurer,
                                          ApplicationContext applicationContext) {
        this.serverProperties = serverProperties;
        this.applicationContext = applicationContext;
        this.resourceProperties = resourceProperties;
        this.viewResolvers = viewResolversProvider.getIfAvailable(() -> Collections.emptyList());
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @ConditionalOnProperty(name = "api.global-error-handler.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public GlobalErrorAttributes errorAttributes(){
        return new GlobalErrorAttributes();
    }

    @Bean
    @ConditionalOnProperty(name = "api.global-error-handler.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(value = ErrorWebExceptionHandler.class, search = SearchStrategy.CURRENT)
    @Order(-2)//必须添加，是为了比 ErrorWebFluxAutoConfiguration 中配置的 DefaultErrorWebExceptionHandler 要早
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes) {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler(errorAttributes, this.resourceProperties,
                this.serverProperties.getError(), this.applicationContext);
        exceptionHandler.setViewResolvers(this.viewResolvers);
        exceptionHandler.setMessageWriters(this.serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(this.serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    @Bean
    @ConditionalOnProperty(name = "api.static-resource.disabled", havingValue = "true", matchIfMissing = true)
    public StaticResourceForbid staticResourceForbid(ResourceProperties resourceProperties){
        resourceProperties.setAddMappings(false);
        return new StaticResourceForbid();
    }

    public class StaticResourceForbid{
    }
}