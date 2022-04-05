package com.xpay.starter.amq.enhance;

import com.xpay.starter.amq.tracer.Tracer;

import javax.jms.Message;
import java.util.Map;

/**
 * 消息轨迹追踪器
 */
public interface Enhancer {
    public void setClientIp(String clientIp);

    public String getClientIp();

    public void setAppName(String appName);

    public String getAppName();

    public void setTracer(Tracer tracer);

    public Tracer getTracer();

    public void trace(Message message, Type type, String flag, Throwable e, Map<String, String> other);

    public enum Type {
        PRODUCE,
        CONSUME
    }
}
