package com.xpay.service.migration;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceMigrationApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceMigrationApp.class).web(WebApplicationType.NONE).run(args);
    }
}
