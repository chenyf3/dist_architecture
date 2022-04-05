package com.xpay.service.sequence;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceSequenceApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceSequenceApp.class).web(WebApplicationType.NONE).build().run(args);
    }
}