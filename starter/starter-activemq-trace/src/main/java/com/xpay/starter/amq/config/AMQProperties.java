package com.xpay.starter.amq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ActiveMQ额外的相关配置，默认情况下，消息消费时的重试间隔为(秒）：2、4、8、16、16、16，即最多共支持62秒内的失败重试，
 * 重试的时候不会阻塞整个队列，而是阻塞监听这条消息的那个线程
 */
@ConfigurationProperties(prefix = "spring.activemq")
public class AMQProperties {
    /**
     * 是否启用消息轨迹
     */
    private boolean traceable = true;
    /**
     * 是否适用ActiveMQ Broker作为轨迹消息的存储介质
     */
    private boolean brokerTrace = true;
    /**
     * 首次重发时间间隔（毫秒）
     */
    private int initialRedeliveryDelay = 2000;
    /**
     * 最大重投次数
     */
    private int maximumRedelivery = 6;
    /**
     * 最大重投间隔时间(毫秒)
     */
    private int maxRedeliveryDelay = 16000;


    public boolean getTraceable() {
        return traceable;
    }

    public void setTraceable(boolean traceable) {
        this.traceable = traceable;
    }

    public boolean getBrokerTrace() {
        return brokerTrace;
    }

    public void setBrokerTrace(boolean brokerTrace) {
        this.brokerTrace = brokerTrace;
    }

    public int getInitialRedeliveryDelay() {
        return initialRedeliveryDelay;
    }

    public void setInitialRedeliveryDelay(int initialRedeliveryDelay) {
        this.initialRedeliveryDelay = initialRedeliveryDelay;
    }

    public int getMaximumRedelivery() {
        return maximumRedelivery;
    }

    public void setMaximumRedelivery(int maximumRedelivery) {
        this.maximumRedelivery = maximumRedelivery;
    }

    public int getMaxRedeliveryDelay() {
        return maxRedeliveryDelay;
    }

    public void setMaxRedeliveryDelay(int maxRedeliveryDelay) {
        this.maxRedeliveryDelay = maxRedeliveryDelay;
    }
}
