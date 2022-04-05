package com.xpay.demo.prometheus;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2017/8/20.
 */
@SpringBootApplication
public class DemoPrometheusApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoPrometheusApp.class).run(args);
    }
}
