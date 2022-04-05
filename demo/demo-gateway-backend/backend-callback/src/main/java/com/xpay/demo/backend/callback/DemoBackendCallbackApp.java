package com.xpay.demo.backend.callback;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DemoBackendCallbackApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoBackendCallbackApp.class).web(WebApplicationType.SERVLET).run(args);
    }
}
