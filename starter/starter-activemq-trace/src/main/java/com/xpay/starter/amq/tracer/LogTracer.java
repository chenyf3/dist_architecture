package com.xpay.starter.amq.tracer;

import com.xpay.starter.amq.config.Const;
import com.xpay.starter.amq.enhance.Enhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

public class LogTracer implements Tracer {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void trace(Message message, Enhancer.Type type, String flag, Throwable e, Map<String, String> other){
        try{
            String traceId = getTraceId(message);
            String trxNo = getTrxNo(message);
            String mchNo = getMchNo(message);
            if(type == Enhancer.Type.PRODUCE){
                if (e == null) {
                    logger.info("traceId={} trxNo={} mchNo={} clientFlag={} {} SUCCESS", traceId, trxNo, mchNo, flag, type);
                }
            }else{
                if (e != null) {
                    logger.error("traceId={} trxNo={} mchNo={} type={} clientFlag={} ERROR", traceId, trxNo, mchNo, type.name(), flag, e);
                }else{
                    logger.info("traceId={} trxNo={} mchNo={} clientFlag={} {} SUCCESS", traceId, trxNo, mchNo, flag, type);
                }
            }
        }catch(Throwable ex){
            logger.error("打印消息trace信息时出现异常", ex);
        }
    }

    public void setTraceId(Message message, String traceId) throws JMSException {
        message.setStringProperty(Const.TRACE_ID, traceId);
    }

    public String getTraceId(Message message) throws JMSException {
        return message.getStringProperty(Const.TRACE_ID);
    }

    public void setTrxNo(Message message, String trxNo) throws JMSException {
        message.setStringProperty(Const.TRACE_TRX_NO, trxNo);
    }

    public String getTrxNo(Message message) throws JMSException {
        return message.getStringProperty(Const.TRACE_TRX_NO);
    }

    public void setMchNo(Message message, String mchNo) throws JMSException {
        message.setStringProperty(Const.TRACE_MCH_NO, mchNo);
    }

    public String getMchNo(Message message) throws JMSException {
        return message.getStringProperty(Const.TRACE_MCH_NO);
    }

    public String getDestination(Message message) throws JMSException {
        return message.getStringProperty(Const.DESTINATION);
    }

    public boolean getIsResend(Message message) throws JMSException {
        String resendStr = message.getStringProperty(Const.TRACE_RESEND);
        if(resendStr != null && "true".equalsIgnoreCase(resendStr)){
            return true;
        }else{
            return false;
        }
    }

    public boolean isNotTrace(Message message){
        try{
            String flag = message.getStringProperty(Const.TRACE_NONE);
            if(flag != null && "true".equals(flag)){
                return true;
            }
        }catch(Exception e){
        }
        return false;
    }

    public int getDeliveryCount(Message message) {
        try{
            return message.getIntProperty("JMSXDeliveryCount");
        }catch(JMSException e){
            return 0;
        }
    }

    /**
     * 打印日志
     * @param prefix            日志前缀
     * @param palaceHolder      占位字符串
     * @param params            参数值
     */
    protected void printError(String prefix, String palaceHolder, Object... params){
        logger.error("[" +prefix+ "]" + palaceHolder, params);
    }
}
