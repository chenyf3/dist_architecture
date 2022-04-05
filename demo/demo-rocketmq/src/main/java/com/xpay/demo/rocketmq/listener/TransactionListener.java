package com.xpay.demo.rocketmq.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xpay.common.utils.RandomUtil;
import com.xpay.demo.rocketmq.bizVo.OrderVo;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

/**
 * 本地事务监听器，功能有2：
 *    1、发送了“半消息”之后，执行本地事务
 *    2、反查本地事务的执行结果，根据本地事务的结果来决定是提交还是回滚“半消息”
 *
 * 整个应用共用同一个监听器，可在executeLocalTransaction和checkLocalTransaction方法里面通过tag来区分不同的业务，又或者自定义
 * 一些字段来区分不同的业务，如定义一个 msgType 字段
 *
 */
@RocketMQTransactionListener
public class TransactionListener implements RocketMQLocalTransactionListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        OrderVo vo = JSONObject.parseObject((byte[])msg.getPayload(), OrderVo.class);

        int rand = RandomUtil.getInt(1, 21);

        if(rand%3 == 0){
            logger.info("COMMIT rand={} OrderVo = {} ", rand, JSON.toJSONString(vo));
            return RocketMQLocalTransactionState.COMMIT;
        }else if(rand%3 == 1){
            logger.info("UNKNOWN rand={} OrderVo = {} ", rand, JSON.toJSONString(vo));
            return RocketMQLocalTransactionState.UNKNOWN;
        }else{
            logger.info("ROLLBACK rand={} OrderVo = {} ", rand, JSON.toJSONString(vo));
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        OrderVo vo = JSONObject.parseObject((byte[])msg.getPayload(), OrderVo.class);

        int rand = RandomUtil.getInt(1, 10);

        if(rand%2 == 0){
            logger.info("COMMIT OrderVo = {} ", JSON.toJSONString(vo));
            return RocketMQLocalTransactionState.COMMIT;
        }else{
            logger.info("ROLLBACK OrderVo = {} ", JSON.toJSONString(vo));
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
