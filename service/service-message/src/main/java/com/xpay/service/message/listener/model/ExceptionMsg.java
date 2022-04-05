package com.xpay.service.message.listener.model;

public class ExceptionMsg {
    private String broker;
    private Throwable throwable;
    private String remark;

    public ExceptionMsg(){}

    public ExceptionMsg(String broker, Throwable throwable, String remark){
        this.broker = broker;
        this.throwable = throwable;
        this.remark = remark;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
