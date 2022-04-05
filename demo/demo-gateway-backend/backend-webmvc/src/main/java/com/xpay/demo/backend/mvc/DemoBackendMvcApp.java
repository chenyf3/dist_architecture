package com.xpay.demo.backend.mvc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DemoBackendMvcApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoBackendMvcApp.class)
                .web(WebApplicationType.SERVLET).run(args);
    }
}
