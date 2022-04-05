package com.xpay.service.message.listener.model;

/**
 * @description 消息队列属性
 * @author linguangsheng
 * @date 2019/03/01
 */
public class QueueProperty {

    /**
     * Broker名称
     */
    private String brokerName;

    /**
     * 队列名
     */
    private String queueName;

    /**
     * 队列剩余的消息数量
     */
    private long queueSize;

    /**
     * 生产者数量
     */
    private long producerCount;

    /**
     * 消费者数量
     */
    private long consumerCount;

    /**
     * 入队消息总数
     */
    private long enqueueCount;

    /**
     * 出队消息总数
     */
    private long dequeueCount;

    /**
     * 消费者是否被暂停
     */
    private boolean paused;

    public static QueueProperty build(){
        return new QueueProperty();
    }

    @Override
    public String toString() {
        return "{" +
                "brokerName:" + brokerName +
                ", queueName:" + queueName +
                ", queueSize:" + queueSize +
                ", producerCount:" + producerCount +
                ", consumerCount:" + consumerCount +
                ", enqueueCount:" + enqueueCount +
                ", dequeueCount:" + dequeueCount +
                ", paused:" + paused +
                "}";
    }

    public String getBrokerName() {
        return brokerName;
    }

    public QueueProperty setBrokerName(String brokerName) {
        this.brokerName = brokerName;
        return this;
    }

    public String getQueueName() {
        return queueName;
    }

    public QueueProperty setQueueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public long getQueueSize() {
        return queueSize;
    }

    public QueueProperty setQueueSize(long queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public long getProducerCount() {
        return producerCount;
    }

    public QueueProperty setProducerCount(long producerCount) {
        this.producerCount = producerCount;
        return this;
    }

    public long getConsumerCount() {
        return consumerCount;
    }

    public QueueProperty setConsumerCount(long consumerCount) {
        this.consumerCount = consumerCount;
        return this;
    }

    public long getEnqueueCount() {
        return enqueueCount;
    }

    public QueueProperty setEnqueueCount(long enqueueCount) {
        this.enqueueCount = enqueueCount;
        return this;
    }

    public long getDequeueCount() {
        return dequeueCount;
    }

    public QueueProperty setDequeueCount(long dequeueCount) {
        this.dequeueCount = dequeueCount;
        return this;
    }

    public boolean getPaused() {
        return paused;
    }

    public QueueProperty setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }
}
