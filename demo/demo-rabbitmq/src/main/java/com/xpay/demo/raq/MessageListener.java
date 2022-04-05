package com.xpay.demo.raq;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.starter.plugin.consts.DeadLetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_DIRECT_1, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)),concurrency = "1-3")
    public void directOneConsume(String msgStr, Message message) {
        logger.info("接收到消费的消息：{}", msgStr);
    }
    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_DIRECT_2, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)),concurrency = "1-3")
    public void directTwoConsume(Message message) {
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到消费的消息：{}", msgStr);
    }
    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_DIRECT_3, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)),concurrency = "1-3")
    public void directThreeConsume(Message message) {
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        MsgDto msgDto = JsonUtil.toBean(msgStr, MsgDto.class);
        logger.info("接收到消费的消息, MsgDto: {}", JsonUtil.toJson(msgDto));
    }


    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_DELAY_1, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void delayOneConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到延迟的消息: {}", msgStr);
    }
    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_DELAY_2, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void delayTwoConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到延迟的消息: {}", msgStr);
    }
    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_DELAY_3, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void delayThreeConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到延迟的消息: {}", msgStr);
    }


    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_FANOUT_1, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void fanoutOneConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到消费的消息: {}", msgStr);
    }
    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_FANOUT_2, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void fanoutTwoConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到消费的消息: {}", msgStr);
    }
    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_FANOUT_3, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void fanoutThreeConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到消费的消息: {}", msgStr);
    }


    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_REDELIVERY, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)), concurrency = "1-3")
    public void directRedelivery(Message message) {
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        MsgDto msgDto = JsonUtil.toBean(msgStr, MsgDto.class);

        MessageProperties properties = message.getMessageProperties();
        logger.info("接收到消费的消息, Redelivered: {} MsgDto: {}", properties.getRedelivered(), JsonUtil.toJson(msgDto));
        throw new BizException("测试消息重投");
    }

    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_REPLY, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public String replyConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("message received: {}", msgStr);

        MsgDto msgDto = new MsgDto();
        msgDto.setTrxNo(RandomUtil.get32LenStr());
        msgDto.setJsonParam("{\"response\":\"reply success\"}");
        return JsonUtil.toJson(msgDto);
    }

    @RabbitListener(queuesToDeclare = @Queue(name=Destinations.QUEUE_TEST_TPS, arguments=@Argument(name=DeadLetter.DLX_ARGS,value=DeadLetter.DLX)))
    public void tpsConsume(Message message){
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到消息: {}", msgStr);
    }


    /**
     * 消费死信队列，一般来说会在人工审核后重新发往Broker
     * @param message
     */
    @RabbitListener(queuesToDeclare = @Queue(name= DeadLetter.DLQ))
    public void dlqConsume(Message message){
        MessageProperties properties = message.getMessageProperties();
        String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("接收到死信队列消息:，body：{}, header：{}", msgStr, properties);

        //TODO 取得源消息的 Exchange、RoutingKey，放入数据库中等待人工审核
        MsgDto parentMsg = JsonUtil.toBean(msgStr, MsgDto.class);
        String oriTopic = parentMsg.getTopic();//源消息的 exchange
        String oriTags = parentMsg.getTags();//源消息的 queue
        String trxNo = parentMsg.getTrxNo();//交易流水号/订单号
        String mchNo = parentMsg.getMchNo();//商户编号
        String oriBody = msgStr;//源消息体(不要使用 parentMsg 序列化成JSON，因为源消息体有可能是MsgDto的子类)
    }
}
