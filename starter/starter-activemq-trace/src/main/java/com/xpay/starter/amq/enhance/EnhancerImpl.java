package com.xpay.starter.amq.enhance;

import com.xpay.starter.amq.tracer.Tracer;

import javax.jms.Message;
import java.util.Map;

public class EnhancerImpl implements Enhancer {
    private String clientIp;
    private String appName;
    private Tracer tracer;

    public void setClientIp(String clientIp){
        this.clientIp = clientIp;
    }

    public String getClientIp(){
        return clientIp;
    }

    public void setAppName(String appName){
        this.appName = appName;
    }

    public String getAppName(){
        return this.appName;
    }

    public void setTracer(Tracer tracer){
        this.tracer = tracer;
    }

    public Tracer getTracer(){
        return this.tracer;
    }

    public void trace(Message message, Type type, String flag, Throwable e, Map<String, String> other){
        if(tracer != null){
            tracer.trace(message, type, genClientFlag(flag), e, other);
        }
    }

    public String genClientFlag(String flag){
        return appName + "#" + flag;
    }
}
