package com.xpay.common.statics.dto.account;

import java.io.Serializable;

public class AccountRequestDto implements Serializable {
    private static final long serialVersionUID = -1636746635745244115L;
    /**
     * 账务处理结果回调目的地(MQ队列名)，如果是RocketMQ的topic、tags，可以用英文的冒号分割，如：my-topic:tags_one
     */
    private String callbackQueue;
    /**
     * 是否需要加急账务处理&加急回调
     */
    private Boolean urgent;

    /**
     * 待账务处理记录Id(接口调用方无需设置，由账务内部自行设置和处理)
     */
    private Long pendingId;

    public boolean isUrgent(){
        return this.urgent != null && this.urgent;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public String getCallbackQueue() {
        return callbackQueue;
    }

    public void setCallbackQueue(String callbackQueue) {
        this.callbackQueue = callbackQueue;
    }

    public Long getPendingId() {
        return pendingId;
    }

    public void setPendingId(Long pendingId) {
        this.pendingId = pendingId;
    }
}
