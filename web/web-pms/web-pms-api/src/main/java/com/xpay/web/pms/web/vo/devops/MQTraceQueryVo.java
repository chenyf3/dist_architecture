package com.xpay.web.pms.web.vo.devops;

public class MQTraceQueryVo {
    private int currentPage;
    private int pageSize;
    private String traceId;//消息Id（轨迹ID）
    private Integer type;//投递类型，1=生产 2=消费
    private Integer msgStatus;//消息状态，1=成功 2=失败
    private String topicGroup;//业务线
    private String topic;//队列名(生产队列)
    private String consumeDest;//消费队列
    private String mchNo;//商户编号
    private String trxNo;//业务流水号
    private Integer resend;//是否补发的消息
    private String msgTimeBegin;//消息时间(开始)
    private String msgTimeEnd;//消息时间(结束)

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(Integer msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getTopicGroup() {
        return topicGroup;
    }

    public void setTopicGroup(String topicGroup) {
        this.topicGroup = topicGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumeDest() {
        return consumeDest;
    }

    public void setConsumeDest(String consumeDest) {
        this.consumeDest = consumeDest;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }

    public Integer getResend() {
        return resend;
    }

    public void setResend(Integer resend) {
        this.resend = resend;
    }

    public String getMsgTimeBegin() {
        return msgTimeBegin;
    }

    public void setMsgTimeBegin(String msgTimeBegin) {
        this.msgTimeBegin = msgTimeBegin;
    }

    public String getMsgTimeEnd() {
        return msgTimeEnd;
    }

    public void setMsgTimeEnd(String msgTimeEnd) {
        this.msgTimeEnd = msgTimeEnd;
    }
}
