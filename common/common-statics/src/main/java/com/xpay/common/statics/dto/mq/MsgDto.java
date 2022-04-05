package com.xpay.common.statics.dto.mq;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息传输对象
 */
public class MsgDto implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String SEPARATOR = ":";

    /**
     * 主题：在RocketMQ中的Topic，在ActiveMQ中的Queue，在RabbitMQ中的Exchange名称和类型，格式为：交换器名:交换器类型，比如： testExchange:direct、testExchange:fanout
     */
    protected String topic;

    /**
     * tags，在RocketMQ中表示tags，在RabbitMQ中表示队列名和RoutingKey(如果有多个队列则使用英文的逗号分割)
     */
    protected String tags;

    /**
     * 交易流水号/业务流水号
     */
    protected String trxNo;

    /**
     * 商户编号
     */
    protected String mchNo;

    /**
     * json格式的参数，非必填
     */
    protected String jsonParam = null;

    /**
     * MQ的消息头以及特殊的自定义消息头，不要使用它来传递业务数据
     */
    protected Map<String, String> header;

    /**
     * 异常信息，比如，发送失败时的异常
     */
    protected Throwable cause;

    public MsgDto(){}

    public MsgDto(String topic, String tags){
        this.topic = topic;
        this.tags = tags;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public String getJsonParam() {
        return jsonParam;
    }

    public void setJsonParam(String jsonParam) {
        this.jsonParam = jsonParam;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void addHeader(String key, String value){
        if(header == null){
            header = new HashMap<>();
        }
        header.put(key, value);
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
