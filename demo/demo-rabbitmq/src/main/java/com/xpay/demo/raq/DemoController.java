package com.xpay.demo.raq;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.plugins.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    MQSender mqSender;

    /**
     * 发送点对点消息，等同于JMS中的Queue
     * @return
     */
    @RequestMapping("sendQueue")
    public String sendQueue(){
        int max = 10;
        for(int i=1; i<max; i++){
            int mod = i % 3;

            String tags;
            if(mod == 0){
                tags = Destinations.QUEUE_TEST_DIRECT_1;
            }else if(mod == 1){
                tags = Destinations.QUEUE_TEST_DIRECT_2;
            }else{
                tags = Destinations.QUEUE_TEST_DIRECT_3;
            }

            MsgDto msg = new MsgDto();
            msg.setTopic(Destinations.EXCHANGE_TEST_DIRECT);
            msg.setTags(tags);
            msg.setTrxNo("100000000001_" + i);
            msg.setJsonParam("{\"key_1\":\"value_1\"}, \"key_2\":\"value_2\"}");
            mqSender.sendOne(msg);
        }
        return "ok";
    }

    /**
     * 发送发布-订阅消息，等同于JMS中的Topic
     * @return
     */
    @RequestMapping("sendTopic")
    public String sendTopic(){
        int max = 10;
        for(int i=1; i<max; i++){
            MsgDto msg = new MsgDto();
            msg.setTopic(Destinations.EXCHANGE_TEST_FANOUT);
            msg.setTags(StringUtil.join(",", Destinations.QUEUE_TEST_FANOUT_1,Destinations.QUEUE_TEST_FANOUT_2,Destinations.QUEUE_TEST_FANOUT_3));
            msg.setTrxNo("200000000002_" + i);
            msg.setJsonParam("{\"key_2\":\"value_2\"}, \"key_3\":\"value_3\"}");
            mqSender.sendOne(msg);
        }
        return "ok";
    }

    /**
     * 发送消息重试消息(需要消费端在消费时抛出异常，让消息重新投递)
     * @return
     */
    @RequestMapping("sendRetry")
    public String sendRetry(){
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.EXCHANGE_TEST_DIRECT);
        msg.setTags(Destinations.QUEUE_TEST_REDELIVERY);
        msg.setTrxNo("100000000001_" + RandomUtil.getInt(1000, 10000));
        msg.setJsonParam("{\"key_1\":\"value_1\"}, \"key_2\":\"value_2\"}");
        mqSender.sendOne(msg);
        return "ok";
    }

    /**
     * 发送延时消息(使用DirectExchange)
     * @return
     */
    @RequestMapping("sendDelay")
    public String sendDelay(){
        int mod = RandomUtil.getInt(1, 100) % 3;

        String tags;
        if(mod == 0){
            tags = Destinations.QUEUE_TEST_DELAY_1;
        }else if(mod == 1){
            tags = Destinations.QUEUE_TEST_DELAY_2;
        }else{
            tags = Destinations.QUEUE_TEST_DELAY_3;
        }

        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.EXCHANGE_TEST_DELAY);
        msg.setTags(tags);
        msg.setTrxNo("300000000003_" + mod);
        msg.setJsonParam("{\"key_3\":\"value_3\"}, \"key_4\":\"value_4\"}");
        mqSender.sendOneDelay(msg, 5);
        return "ok: " + tags;
    }

    @RequestMapping("sendReply")
    public String sendReply(){
        MsgDto msg = new MsgDto();
        msg.setTopic(Destinations.EXCHANGE_TEST_DIRECT);
        msg.setTags(Destinations.QUEUE_TEST_REPLY);
        msg.setTrxNo("100000000001_" + RandomUtil.getInt(1000, 10000));
        msg.setJsonParam("{\"key_1\":\"value_1\"}, \"key_2\":\"value_2\"}");
        MsgDto respMsg = mqSender.sendAndReceive(msg);
        return "ok, respMsg : " + JsonUtil.toJson(respMsg);
    }

    /**
     * 简单的测一下tps
     * @return
     */
    @RequestMapping("sendTPS")
    public String sendTPS(){
        long start = System.currentTimeMillis();
        int total = 300000;
        for(int i=1; i<=total; i++){
            MsgDto msg = new MsgDto();
            msg.setTopic(Destinations.EXCHANGE_TEST_TPS);
            msg.setTags(Destinations.QUEUE_TEST_TPS);
            msg.setTrxNo("100000000001_" + i);
            msg.setJsonParam("{\"key_1\":\"value_1\"}, \"key_2\":\"value_2\"}");
            mqSender.sendOne(msg);
        }
        long cost = (System.currentTimeMillis() - start)/1000L;
        int tps = (int) (total/cost);
        return "SUCCESS!! total: " + total + ", cost: " + cost + "(秒), tps: " + tps;
    }
}
