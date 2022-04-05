package com.xpay.starter.monitor.config;

import com.xpay.starter.monitor.filter.MvcManagementEndpointSecurityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Endpoint管理端点的配置类
 */
@ManagementContextConfiguration
public class SecurityManagementContextConfiguration {
    @Value("${management.endpoints.web.base-path}")
    private String manageEndpointPath;
    @Value("${management.endpoints.web.token-header:Authorization}")
    private String tokenHeader;
    @Value("${management.endpoints.web.permit-tokens:09167d690d4f10d18bf34f4ea596e5e7}")
    private String permitTokens;

    /**
     * 注入安全校验的过滤器
     * @return
     */
    @Bean
    public FilterRegistrationBean<MvcManagementEndpointSecurityFilter> mvcManagementEndpointSecurityFilterFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new MvcManagementEndpointSecurityFilter(tokenHeader, permitTokens));

        String urlPattern = manageEndpointPath + "/*";//只对 Manage Endpoint 的访问url执行
        registration.addUrlPatterns(urlPattern);
        registration.setName(MvcManagementEndpointSecurityFilter.class.getSimpleName());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
