package com.xpay.service.accountmch;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceAccountMchApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceAccountMchApp.class).web(WebApplicationType.NONE).run(args);
    }
}
