package com.xpay.demo.zookeeper;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class DemoZookeeperApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoZookeeperApp.class).run(args);
    }
}
