package com.xpay.demo.sentinel.provider;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2017/8/20.
 */
@SpringBootApplication
public class DemoSentinelProviderApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoSentinelProviderApp.class).run(args);
    }
}
