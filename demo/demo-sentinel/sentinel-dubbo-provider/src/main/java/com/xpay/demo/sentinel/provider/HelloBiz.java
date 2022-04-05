package com.xpay.demo.sentinel.provider;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Component;

@Component
public class HelloBiz {

    public String sayHello(){
        return "Hello";
    }


    public String helloWorld(){
        return "Hello World!";
    }

    @SentinelResource(value = "testHello", entryType = EntryType.IN, blockHandler = "testHelloBlock")
    public String testHello(){
        return "test Hello";
    }
    public String testHelloBlock(BlockException e){
        return "blocked!";
    }
}
