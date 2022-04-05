package com.xpay.service.mchnotify;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 商户通知服务
 */
@SpringBootApplication
public class ServiceMchNotifyApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceMchNotifyApp.class).web(WebApplicationType.NONE).run(args);
    }
}
