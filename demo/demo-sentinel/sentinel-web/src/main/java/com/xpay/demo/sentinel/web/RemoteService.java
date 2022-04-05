package com.xpay.demo.sentinel.web;

import org.springframework.stereotype.Service;

@Service
public class RemoteService {

    public String call(){
        try {
            Thread.sleep(320);//模拟接口处理业务的耗时
        } catch (Exception e){
            e.printStackTrace();
        }
        return "ok";
    }
}
