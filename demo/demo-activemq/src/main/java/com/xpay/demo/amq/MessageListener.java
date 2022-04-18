package com.xpay.demo.amq;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import org.apache.activemq.Message;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MessageListener {

    @JmsListener(destination = Destinations.TEST_TRACE, concurrency = "3-6")
    public void consumeTrace(String msgStr, Message message) throws Exception {
        int rand = RandomUtil.getInt(6);
        if(rand % 2 == 0){
            throw new BizException(DateUtil.formatDateTimeMills(new Date()) + " DeliveryCount = " + message.getIntProperty("JMSXDeliveryCount") + ", DEBUG 测试消息重投, message = " + msgStr);
        }

        System.out.println("接收到消费的消息：" + msgStr);
    }

    @JmsListener(destination = Destinations.TEST_TRACE_2)
    public void consumeTrace2(String message){
        System.out.println("接收到消费的消息：" + message);
    }

    @JmsListener(destination = Destinations.TEST_DELAY)
    public void consumeDelay(String message){
        System.out.println("接收到延迟的消息：" + message);
    }

    @JmsListener(destination = Destinations.TEST_REPLY)
    public String consumeReply(String message){
        System.out.println("接收到需要回复的消息：" + message);

        MsgDto msgDto = new MsgDto();
        msgDto.setTrxNo(RandomUtil.get32LenStr());
        msgDto.addParam("response", "reply success");
        return JsonUtil.toJson(msgDto);
    }

    @JmsListener(destination = Destinations.FAILOVER, concurrency = "1-1")
    public void consumeFailover(String message){
        System.out.println("接收到Failover消费的消息：" + message);

        //模拟业务处理时间
        try{
            Thread.sleep(500);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "Consumer.A." + Destinations.VIRTUAL, concurrency = "1-10")
    public void consumeVirtualA(String message){
        System.out.println("接收到Virtual消费的消息A：" + message);
    }

    @JmsListener(destination = "Consumer.B." + Destinations.VIRTUAL, concurrency = "1-10")
    public void consumeVirtualB(String message){
        System.out.println("接收到Virtual消费的消息B：" + message);
    }

    @JmsListener(destination = Destinations.TEST_BRIDGE)
    public void consumeBridge(String message){
        System.out.println("接收到Bridge消费的消息" + message);
    }

    @JmsListener(destination = "Consumer.A." + Destinations.TEST_VTOPIC_BRIDGE)
    public void consumeBridgeVirtualA(String message){
        System.out.println("接收到Bridge_VTopic消费的消息A" + message);
    }

    @JmsListener(destination = "Consumer.B." + Destinations.TEST_VTOPIC_BRIDGE)
    public void consumeBridgeVirtualB(String message){
        System.out.println("接收到Bridge_VTopic消费的消息B" + message);
    }
}
