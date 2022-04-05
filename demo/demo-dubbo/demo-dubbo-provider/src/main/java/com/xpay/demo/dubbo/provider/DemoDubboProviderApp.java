package com.xpay.demo.dubbo.provider;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoDubboProviderApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoDubboProviderApp.class).run(args);
    }
}
