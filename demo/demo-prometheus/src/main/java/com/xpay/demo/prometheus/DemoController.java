package com.xpay.demo.prometheus;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @RequestMapping("hello")
    public String hello(){
        return "{\"hello\": \"world\"}";
    }
}
