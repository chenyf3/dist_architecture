package com.xpay.demo.backend.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wechatPay")
public class WechatPayController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "payFinish")
    public String payFinish(@RequestBody String callbackJson){
        logger.info("callbackJson = {}", callbackJson);

        return "ok";
    }
}
