package com.xpay.demo.lo4j2;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2021/8/20.
 */
@SpringBootApplication
public class DemoLoggingApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoLoggingApp.class).run(args);
    }
}
