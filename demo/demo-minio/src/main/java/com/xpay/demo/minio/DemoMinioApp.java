package com.xpay.demo.minio;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2020/8/20.
 */
@SpringBootApplication
public class DemoMinioApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoMinioApp.class).run(args);
    }
}
