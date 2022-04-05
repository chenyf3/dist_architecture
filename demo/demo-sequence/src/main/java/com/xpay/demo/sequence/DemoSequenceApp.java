package com.xpay.demo.sequence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoSequenceApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoSequenceApp.class).run(args);
    }
}
