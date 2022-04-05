package com.xpay.demo.backend.nosentinel;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DemoBackendNoSentinelApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoBackendNoSentinelApp.class).run(args);
    }
}
