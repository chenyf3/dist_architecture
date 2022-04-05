package com.xpay.starter.amq.config;

public class Const {
    public final static String TRACE_ID = "TRC.ID";//消息轨迹 -> 轨迹ID
    public final static String TRACE_TRX_NO = "TRC.TRX_NO";//消息轨迹 -> 交易流水号
    public final static String TRACE_MCH_NO = "TRC.MCH_NO";//消息轨迹 -> 商户编号
    public final static String TRACE_RESEND = "TRC.RESEND";//消息轨迹 -> 是否补发消息
    public final static String TRACE_QUEUE_NAME = "message.mqtrace.queue";//消息轨迹 -> 轨迹消息的队列名
    public final static String TRACE_NONE = "TRC.NONE";//消息轨迹 -> 不使用轨迹追踪时设置

    public final static String DESTINATION = "DESTINATION";//源消息目的地
    public final static String CONSUME_DEST = "consumeDest";//消息最终的消费队列(使用虚拟队列时，发送队列名和消费队列名不一样)
}
