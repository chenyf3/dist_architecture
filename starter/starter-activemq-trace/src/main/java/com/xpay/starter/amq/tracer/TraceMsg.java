package com.xpay.starter.amq.tracer;

import java.io.Serializable;
import java.util.Date;

public class TraceMsg implements Serializable {
    public final static int STATUS_OK = 1;
    public final static int STATUS_FAIL = 2;

    private String traceId;
    private String trxNo;
    private String mchNo;
    private Date msgTime;
    private String destination;//生产端队列名
    private String consumeDest;//消费端的队列名(使用ActiveMQ的虚拟队列时会出现生产者的队列名和消费者的队列名不一样的情况)
    private String type;
    private String client;
    private int status;//消息状态：1=发送或消费成功，2=发送或消费失败
    private int deliveryCount;
    private boolean resend;
    private String errMsg;
    private String oriMsg;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public Date getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(Date msgTime) {
        this.msgTime = msgTime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getConsumeDest() {
        return consumeDest;
    }

    public void setConsumeDest(String consumeDest) {
        this.consumeDest = consumeDest;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(int deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    public boolean getResend() {
        return resend;
    }

    public void setResend(boolean resend) {
        this.resend = resend;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getOriMsg() {
        return oriMsg;
    }

    public void setOriMsg(String oriMsg) {
        this.oriMsg = oriMsg;
    }
}
