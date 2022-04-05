package com.xpay.service.timer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2020/8/20.
 */
@SpringBootApplication
public class ServiceTimerApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(ServiceTimerApp.class).web(WebApplicationType.NONE).build().run(args);
    }
}
