package com.xpay.demo.image;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2017/8/20.
 */
@SpringBootApplication
public class DemoImageApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoImageApp.class).run(args);
    }
}
