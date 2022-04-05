package com.xpay.service.channel;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceChannelApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceChannelApp.class).web(WebApplicationType.NONE).run(args);
    }
}
