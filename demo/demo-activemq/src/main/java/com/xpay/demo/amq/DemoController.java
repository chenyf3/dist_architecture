package com.xpay.demo.amq;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.starter.plugin.plugins.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    MQSender mqSender;

    @RequestMapping("send")
    public String send(){
        int max = 100;
        for(int i=1; i<max; i++){
            MsgDto msg = new MsgDto();
            msg.setTopic(Destinations.TEST_TRACE);
            msg.setTags("topicGroup");//使用此字段做业务分组 需要消息轨迹追踪时可以用上
            msg.setTrxNo("100000000001" + "_" + i);
            msg.setJsonParam("{\"key_1\":\"value_1\"}, \"key_2\":\"value_2\"}");
            mqSender.sendOne(msg);
        }
        return "ok";
    }

    @RequestMapping("send2")
    public String send2(){
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.TEST_TRACE_2);
        msg.setTags("topicGroup");//使用此字段做业务分组 需要消息轨迹追踪时可以用上
        msg.setTrxNo("100000000001");
        msg.setJsonParam("{\"key_2\":\"value_2\"}, \"key_3\":\"value_3\"}");
        mqSender.sendOne(msg);

        return "ok";
    }

    @RequestMapping("sendVirtual")
    public String sendVirtual(){
        for(int i=0; i<1; i++){
            MsgDto msg = new MsgDto();
            msg.setTopic(Destinations.VIRTUAL);
            msg.setTags("topicGroup");//使用此字段做业务分组 需要消息轨迹追踪时可以用上
            msg.setTrxNo("100000000005");
            msg.setJsonParam("{\"key_3\":\"value_3\"}, \"key_4\":\"value_4\"}");
            mqSender.sendOne(msg);
        }
        return "ok";
    }

    @RequestMapping("sendDelay")
    public String sendDelay(){
        for(int i=0; i<1; i++){
            MsgDto msg = new MsgDto();
            msg.setTopic(Destinations.TEST_DELAY);
            msg.setTags("topicGroup");//使用此字段做业务分组 需要消息轨迹追踪时可以用上
            msg.setTrxNo("100000000007");
            msg.setJsonParam("{\"key_5\":\"value_5\"}, \"key_5\":\"value_5\"}");
            mqSender.sendOneDelay(msg, 30);
        }
        return "ok";
    }

    @RequestMapping("sendReply")
    public String sendReply(){
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.TEST_REPLY);
        msg.setTags("topicGroup");
        msg.setTrxNo("100000000008_" + RandomUtil.getInt(1000, 10000));
        msg.setJsonParam("{\"key_1\":\"value_1\"}, \"key_2\":\"value_2\"}");
        MsgDto respMsg = mqSender.sendAndReceive(msg);
        return "ok, respMsg : " + JsonUtil.toJson(respMsg);
    }

    /**
     * 测试failOver，启动时会连接到第一个Broker，然后我们开始发送&消费消息，之后把第一个Broker停掉，观察是否会切换到第二个Broker进行发送&消费
     * @return
     */
    @RequestMapping("failOver")
    public String failOver(){
        String trxNo = "100000000001_";
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.FAILOVER);
        msg.setTags("topicGroup_2");//使用此字段做业务分组 需要消息轨迹追踪时可以用上
        msg.setJsonParam("{\"key_2\":\"value_2\"}, \"key_3\":\"value_3\"}");
        for(int i=0; i<100000; i++){
            try{
                msg.setTrxNo(trxNo + i);
                mqSender.sendOne(msg);

                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return "ok";
    }

    @RequestMapping("sendBridge")
    public String sendBridge(){
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.TEST_BRIDGE);
        msg.setTags("topicGroup"); //使用此字段做业务分组 需要消息轨迹追踪时可以用上
        msg.setTrxNo("600000000001");
        msg.setJsonParam("{\"bridge_key_1\":\"bridge_value_1\"}, \"bridge_key_2\":\"bridge_value_2\"}");
        if(mqSender.sendOne(msg)){
            return "ok";
        }else{
            return "fail";
        }
    }

    @RequestMapping("sendVTopicBridge")
    public String sendVTopicBridge(){
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.TEST_VTOPIC_BRIDGE);
        msg.setTags("topicGroup"); //使用此字段做业务分组 需要消息轨迹追踪时可以用上
        msg.setTrxNo("700000000001");
        msg.setJsonParam("{\"vtopic_bridge_key_1\":\"vtopic_bridge_value_1\"}, \"vtopic_bridge_key_2\":\"vtopic_bridge_value_2\"}");
        if(mqSender.sendOne(msg)){
            return "ok";
        }else{
            return "fail";
        }
    }
}
