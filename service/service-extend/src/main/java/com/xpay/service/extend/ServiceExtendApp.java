package com.xpay.service.extend;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceExtendApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceExtendApp.class).web(WebApplicationType.NONE).run(args);
    }
}