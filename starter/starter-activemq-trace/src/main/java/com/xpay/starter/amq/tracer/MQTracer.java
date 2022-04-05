package com.xpay.starter.amq.tracer;

import com.xpay.starter.amq.config.Const;
import com.xpay.starter.amq.enhance.Enhancer;
import com.xpay.starter.amq.util.WrapperUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.listener.adapter.ListenerExecutionFailedException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MQTracer extends LogTracer {
    private final String TRACE_FLAG = "TRC.T";
    private ConnectionFactory connectionFactory;
    private DestinationResolver destinationResolver = new DynamicDestinationResolver();

    public MQTracer(ConnectionFactory connectionFactory){
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void trace(Message message, Enhancer.Type type, String flag, Throwable e, Map<String, String> other) {
        //如果是消息生产端发送失败，则打印日志且不再发送消息轨迹，如果当前消息已经是轨迹消息，也不再发送，避免进入死循环
        if(type == Enhancer.Type.PRODUCE && e != null){
            printError("AMQ_SEND_FAIL", " Exception：{}， MessageBody: {}", e.getMessage(), getOriMsg(message));
            return;
        }else if(this.isTraceMsg(message) || super.isNotTrace(message)){
            return;
        }

        CompletableFuture.runAsync(() -> sendTrace(message, type, flag, e, other));
    }

    private void sendTrace(Message message, Enhancer.Type type, String flag, Throwable e, Map<String, String> other){
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try{
            TraceMsg traceMsg = new TraceMsg();
            traceMsg.setTraceId(getTraceId(message));
            traceMsg.setTrxNo(getTrxNo(message));
            traceMsg.setMchNo(getMchNo(message));
            traceMsg.setMsgTime(new Date());
            traceMsg.setDestination(getDestination(message));
            if(other != null && ! other.isEmpty()){
                if(type == Enhancer.Type.CONSUME){
                    traceMsg.setConsumeDest(other.get(Const.CONSUME_DEST));
                }
            }
            traceMsg.setType(type.name());
            traceMsg.setClient(flag);
            traceMsg.setStatus(e==null ? TraceMsg.STATUS_OK : TraceMsg.STATUS_FAIL);
            traceMsg.setDeliveryCount(getDeliveryCount(message));
            traceMsg.setResend(getIsResend(message));
            traceMsg.setErrMsg(e==null ? null : getErrMsg(e));
            if(Enhancer.Type.CONSUME == type && message.getJMSRedelivered()){
                traceMsg.setOriMsg(null);//如果是消息的重投，则不再发送原消息体到Broker去，以减小Broker端的压力
            }else{
                traceMsg.setOriMsg(getOriMsg(message));
            }
            connection = connectionFactory.createConnection();
            session = connection.createSession();
            Destination destination = destinationResolver.resolveDestinationName(session, Const.TRACE_QUEUE_NAME, false);
            Message traceJmsMsg = session.createTextMessage(WrapperUtil.toJSONString(traceMsg));
            traceJmsMsg.setStringProperty(TRACE_FLAG, "1");//设置轨迹消息的标识
            producer = session.createProducer(destination);
            //发送非持久化、不过期消息(即只要MQ服务器不宕机，消息就不丢)
            producer.send(traceJmsMsg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, producer.getTimeToLive());
        } catch(Throwable ex){
            logger.error("发送trace信息时出现异常", ex);
        } finally {
            JmsUtils.closeMessageProducer(producer);
            JmsUtils.closeSession(session);
            ConnectionFactoryUtils.releaseConnection(connection, connectionFactory, false);
        }
    }

    private boolean isTraceMsg(Message message){
        try{
            String flag = message.getStringProperty(TRACE_FLAG);
            if(flag != null && flag.trim().length() > 0){
                return true;
            }
            return false;
        }catch(Exception e){
            return true;//如果出现异常，直接返回true，避免进入死循环
        }
    }

    private String getErrMsg(Throwable e){
        if(e instanceof ListenerExecutionFailedException){
            return e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
        }else{
            return e.getClass().getName() + ": " + e.getMessage();
        }
    }

    private String getOriMsg(Message message){
        try{
            if(message instanceof ActiveMQTextMessage){
                return ((ActiveMQTextMessage)message).getText();
            }else{
                return message.getBody(String.class);
            }
        }catch(Throwable e){
            return "{\"getOriMsgErr\" : \"" + e.getMessage() + "\"}";
        }
    }
}
