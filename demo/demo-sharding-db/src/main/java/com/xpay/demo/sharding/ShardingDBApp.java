package com.xpay.demo.sharding;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ShardingDBApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ShardingDBApp.class).run(args);
    }
}