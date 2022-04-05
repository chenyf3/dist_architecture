package com.xpay.service.message;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceMessageApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceMessageApp.class).web(WebApplicationType.NONE).run(args);
    }
}