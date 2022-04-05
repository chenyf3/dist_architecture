package com.xpay.demo.rocketmq;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2017/8/20.
 */
@SpringBootApplication
public class DemoRmqApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoRmqApp.class).run(args);
    }
}
