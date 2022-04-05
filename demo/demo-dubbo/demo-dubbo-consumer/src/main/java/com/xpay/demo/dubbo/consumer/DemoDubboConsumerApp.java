package com.xpay.demo.dubbo.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoDubboConsumerApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoDubboConsumerApp.class).run(args);
    }
}
