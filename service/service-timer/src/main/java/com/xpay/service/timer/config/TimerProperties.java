package com.xpay.service.timer.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "timer")
public class TimerProperties {
    /**
     * 是否需要记录操作日志
     */
    private boolean opLogEnable = true;

    private Map<String, String> quartz = new HashMap<>();

    public boolean getOpLogEnable() {
        return opLogEnable;
    }

    public void setOpLogEnable(boolean opLogEnable) {
        this.opLogEnable = opLogEnable;
    }

    public Map<String, String> getQuartz() {
        return quartz;
    }

    public void setQuartz(Map<String, String> quartz) {
        this.quartz = quartz;
    }
}
