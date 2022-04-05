package com.xpay.demo.lo4j2;

import com.xpay.common.statics.constants.common.RemoteLogger;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {
    private Logger httpLogger = LoggerFactory.getLogger(RemoteLogger.HTTP);
    private Logger smtpLogger = LoggerFactory.getLogger(RemoteLogger.SMTP);
    private Logger jdbcLogger = LoggerFactory.getLogger(RemoteLogger.JDBC);
    private Logger failoverLogger = LoggerFactory.getLogger(RemoteLogger.FAILOVER);

    @RequestMapping("http")
    public String http(){
        for(int i=0; i<2; i++){
            Map<String, Object> data = new HashMap<>();
            data.put("key_01", "val_01");
            data.put("key_02", Double.valueOf(20.30));
            data.put("key_03", 800);
            data.put("key_04", Boolean.TRUE);
            data.put("key_05", "http日志消息 rand=" + RandomUtil.getInt(1, 200));
            httpLogger.info(MarkerFactory.getMarker("HTTP_DEMO"), JsonUtil.toJson(data), new RuntimeException("异常信息dddd"));
        }
        return "http";
    }

    @RequestMapping("smtp")
    public String smtp(){
        smtpLogger.info(MarkerFactory.getMarker("SMTP_DEMO"), "smtp日志消息 rand={}", RandomUtil.getInt(1, 200));
        return "smtp";
    }

    @RequestMapping("jdbc")
    public String jdbc(){
        jdbcLogger.info(MarkerFactory.getMarker("JDBC_DEMO"), "jdbc日志消息 rand={}", RandomUtil.getInt(1, 200));
        return "jdbc";
    }

    @RequestMapping("failover")
    public String failover(){
        failoverLogger.info(MarkerFactory.getMarker("FAILOVER_DEMO"), "failover日志消息 rand={}", RandomUtil.getInt(1, 200));
        return "failover";
    }
}
