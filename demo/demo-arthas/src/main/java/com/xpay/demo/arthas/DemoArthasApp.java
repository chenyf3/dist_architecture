package com.xpay.demo.arthas;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2020/8/20.
 */
@SpringBootApplication
public class DemoArthasApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoArthasApp.class).run(args);
    }
}
