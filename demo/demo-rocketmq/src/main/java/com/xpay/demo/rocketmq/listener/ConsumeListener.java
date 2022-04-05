package com.xpay.demo.rocketmq.listener;

import com.alibaba.fastjson.JSON;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.demo.rocketmq.Destinations;
import com.xpay.demo.rocketmq.bizVo.OrderVo;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQReplyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsumeListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_SIMPLE, selectorExpression = Destinations.TAGS_ONE, consumeThreadMax = 2, consumerGroup = "simpleTagOneConsumer")
    public class SimpleTagOneConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("simple_tag_one_consume OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_SIMPLE, selectorExpression = Destinations.TAGS_TWO, consumeThreadMax = 2, consumerGroup = "simpleTagTwoConsumer")
    public class SimpleTagTwoConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("simple_tag_two_consume OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_BATCH, selectorExpression = Destinations.TAGS_ONE, consumerGroup = "batchTagOneConsumer")
    public class BatchTagOneConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("batch_tag_one_consume OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_BATCH, selectorExpression = Destinations.TAGS_TWO, consumerGroup = "batchTagTwoConsumer")
    public class BatchTagTwoConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("batch_tag_two_consume OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_TRANS, selectorExpression = Destinations.TAGS_ONE, consumerGroup = "transTagOneConsumer")
    public class TransTagOneConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_TRANS, selectorExpression = Destinations.TAGS_TWO, consumerGroup = "transTagTwoConsumer")
    public class TransTagTwoConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_DELAY, selectorExpression = Destinations.TAGS_ONE, consumerGroup = "delayTagOneConsumer")
    public class DelayTagOneConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = Destinations.TOPIC_REPLY, consumerGroup = "ReplyConsumer")
    public class ReplyConsumer implements RocketMQReplyListener<MsgDto, MsgDto> {
        @Override
        public MsgDto onMessage(MsgDto message) {
            logger.info("ReplyConsumer received = {}", JsonUtil.toJson(message));

            MsgDto msgDto = new MsgDto();
            msgDto.setJsonParam("{\"msg\": \"消费端已收到消息\"}");
            return msgDto;
        }
    }

    /**
     * 消费顺序消息，顺序消息要求发送端也能做到有序性(全局有序或局部有序)
     */
    @Component
    @RocketMQMessageListener(
            topic = Destinations.TOPIC_ORDERLY,
            selectorExpression = Destinations.TAGS_ONE,
            consumeMode = ConsumeMode.ORDERLY,
            messageModel = MessageModel.CLUSTERING,
            consumeThreadMax = 1,
            consumerGroup = "orderlyConsumer"
    )
    public class OrderlyConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }
}
