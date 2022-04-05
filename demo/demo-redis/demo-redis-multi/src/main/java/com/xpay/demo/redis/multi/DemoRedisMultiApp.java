package com.xpay.demo.redis.multi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author chenyf on 2017/8/20.
 */
@EnableCaching//需要用到 @Cacheable 注解时要加此注解
@SpringBootApplication
public class DemoRedisMultiApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(DemoRedisMultiApp.class).run(args);
    }
}
