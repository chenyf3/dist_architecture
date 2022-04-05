package com.xpay.demo.oss;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2022/3/2.
 */
@SpringBootApplication
public class DemoOSSApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoOSSApp.class).run(args);
    }
}
