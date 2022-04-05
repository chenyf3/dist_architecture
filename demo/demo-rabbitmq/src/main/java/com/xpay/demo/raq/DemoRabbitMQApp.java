package com.xpay.demo.raq;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2020/8/20.
 */
@SpringBootApplication
public class DemoRabbitMQApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoRabbitMQApp.class).run(args);
    }
}
