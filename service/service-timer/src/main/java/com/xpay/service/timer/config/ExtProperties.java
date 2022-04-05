package com.xpay.service.timer.config;

public class ExtProperties {
    private String instanceId;
    private String rpcAddress;
    private Integer checkInInterval = 5000;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getRpcAddress() {
        return rpcAddress;
    }

    public void setRpcAddress(String rpcAddress) {
        this.rpcAddress = rpcAddress;
    }

    public Integer getCheckInInterval() {
        return checkInInterval;
    }

    public void setCheckInInterval(Integer checkInInterval) {
        this.checkInInterval = checkInInterval;
    }
}
