package com.xpay.demo.skywalking;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoSkywalkingApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoSkywalkingApp.class).run(args);
    }
}
