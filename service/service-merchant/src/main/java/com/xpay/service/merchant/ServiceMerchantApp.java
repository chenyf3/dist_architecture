package com.xpay.service.merchant;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Description: 商户管理服务APP
 */
@EnableCaching
@SpringBootApplication
public class ServiceMerchantApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceMerchantApp.class).web(WebApplicationType.NONE).run(args);
    }
}
