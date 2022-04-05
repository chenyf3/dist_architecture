package com.xpay.demo.amq;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2020/8/20.
 */
@SpringBootApplication
public class DemoActiveMQApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoActiveMQApp.class).run(args);
    }
}
