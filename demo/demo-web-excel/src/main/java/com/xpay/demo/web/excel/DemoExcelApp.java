package com.xpay.demo.web.excel;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author chenyf on 2017/8/20.
 */
@SpringBootApplication
public class DemoExcelApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoExcelApp.class).run(args);
    }
}
