package com.xpay.starter.amq.tracer;

import com.xpay.starter.amq.enhance.Enhancer;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

public interface Tracer {
    public void setTraceId(Message message, String traceId) throws JMSException;

    public void setTrxNo(Message message, String trxNo) throws JMSException;

    public String getTraceId(Message message) throws JMSException;

    public String getTrxNo(Message message) throws JMSException;

    public int getDeliveryCount(Message message);

    public void trace(Message message, Enhancer.Type type, String flag, Throwable e, Map<String, String> other);
}
