package com.xpay.service.user;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceUserApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceUserApp.class)
                .web(WebApplicationType.NONE)
                .build()
                .run(args);
    }
}
