package com.xpay.demo.backend.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alipay")
public class AlipayController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "payFinish")
    public String payFinish(@RequestBody String callbackJson){
        logger.info("callbackJson = {}", callbackJson);

        if(true){
//            throw BizException.bizFailResp("", "测试controller中抛出的Biz异常");
//            throw new RuntimeException("测试controller中抛出系统异常");
        }

        return "ok";
    }
}
