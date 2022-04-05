package com.xpay.service.config;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceBaseConfigApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceBaseConfigApp.class).web(WebApplicationType.NONE).run(args);
    }
}