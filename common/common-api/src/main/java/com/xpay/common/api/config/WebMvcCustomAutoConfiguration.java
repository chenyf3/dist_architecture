package com.xpay.common.api.config;

import com.xpay.common.api.webmvc.GlobalErrorAttributes;
import com.xpay.common.api.webmvc.GlobalExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.xpay.common.api.webmvc.ServletRequestFilter;

import javax.servlet.Servlet;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)//因为 ErrorAttributes 要比ErrorMvcAutoConfiguration先生效
public class WebMvcCustomAutoConfiguration {
    private ServerProperties serverProperties;
    private List<ErrorViewResolver> errorViewResolvers;

    public WebMvcCustomAutoConfiguration(ServerProperties serverProperties, ObjectProvider<ErrorViewResolver> errorViewResolvers) {
        this.serverProperties = serverProperties;
        this.errorViewResolvers = errorViewResolvers.orderedStream().collect(Collectors.toList());
    }

    @Bean
    @ConditionalOnProperty(name = "api.global-error-handler.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public ErrorAttributes errorAttributes() {
        return new GlobalErrorAttributes();
    }

    @Bean
    @ConditionalOnProperty(name = "api.global-error-handler.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
    public GlobalExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes){
        return new GlobalExceptionHandler(errorAttributes, this.serverProperties.getError(), this.errorViewResolvers);
    }

    @ConditionalOnProperty(name = "api.servlet-wrapper-filter.enabled", havingValue = "true")
    @Bean
    public ServletRequestFilter servletRequestFilter(){
        return new ServletRequestFilter();
    }

    /**
     * 注册请求过滤器
     * @return
     */
    @Bean
    @ConditionalOnBean(ServletRequestFilter.class)
    public FilterRegistrationBean requestFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(servletRequestFilter());
        registration.setEnabled(true); // 设置是否可用
        return registration;
    }
}
