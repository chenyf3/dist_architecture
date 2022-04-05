package com.xpay.demo.sentinel.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2017/8/20.
 */
@SpringBootApplication
public class DemoSentinelWebApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoSentinelWebApp.class).run(args);
    }
}
