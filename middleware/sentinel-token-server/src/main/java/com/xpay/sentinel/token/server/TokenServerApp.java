package com.xpay.sentinel.token.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TokenServerApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TokenServerApp.class).run(args);
    }
}
