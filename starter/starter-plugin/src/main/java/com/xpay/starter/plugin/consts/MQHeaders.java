package com.xpay.starter.plugin.consts;

public class MQHeaders {
    /**--------------- 这几个用以消息轨迹的消息头，需要和 com.xpay.starter.amq.config.Const 中的保持一致 ---*/
    public final static String TRACE_ID = "TRC.ID";//消息轨迹 -> 轨迹ID
    public final static String TRACE_TRX_NO = "TRC.TRX_NO";//消息轨迹 -> 交易流水号
    public final static String TRACE_MCH_NO = "TRC.MCH_NO";//消息轨迹 -> 商户编号
    public final static String TRACE_RESEND = "TRC.RESEND";//消息轨迹 -> 是否补发消息
    public final static String TRACE_NONE = "TRC.NONE";//消息轨迹 -> 不使用轨迹追踪
    /**--------------- 这几个用以消息轨迹的消息头，需要和 com.xpay.starter.amq.config.Const 中的保持一致 ---*/

    public final static String DESTINATION = "DESTINATION";
    public final static String SCHEDULED_DELAY = "SCHEDULED_DELAY";
}
