package com.xpay.service.timer.biz;

import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.HttpUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.plugins.MQSender;
import com.xpay.facade.timer.dto.JobInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

/**
 * 任务被触发后发送通知
 * @author chenyf
 */
@Component
public class NotifyBiz {
    private Logger logger = LoggerFactory.getLogger(NotifyBiz.class);

    @Autowired(required = false)
    @Qualifier(value = "rmqSender")
    private MQSender rmqSender;

    @Autowired(required = false)
    @Qualifier(value = "amqSender")
    private MQSender amqSender;

    @Autowired(required = false)
    @Qualifier(value = "raqSender")
    private MQSender raqSender;

    public boolean jobNotify(JobInfoDto jobInfo) throws BizException {
        if(jobInfo.isRocketMQDestination()){
            return this.sendRocketMQ(jobInfo);
        }else if(jobInfo.isActiveMQDestination()){
            return this.sendActiveMQ(jobInfo);
        }else if(jobInfo.isRabbitMQDestination()){
            return this.sendRabbitMQ(jobInfo);
        }else if(jobInfo.isHttpDestination()){
            return this.sendHttp(jobInfo);
        }else{
            logger.error("没有匹配到通知发送类型，消息通知发送失败！ jobGroup={} jobName={} destination={}",
                    jobInfo.getJobGroup(), jobInfo.getJobName(), jobInfo.getDestination());
            return false;
        }
    }

    private boolean sendRocketMQ(JobInfoDto jobInfo) {
        if (rmqSender == null) {
            throw new BizException(BizException.BIZ_INVALID, "无法发送RocketMQ信息，请检查RocketMQ相关配置信息");
        }

        try {
            MsgDto msg = new MsgDto();
            msg.setTrxNo(this.buildTrxNo(jobInfo));
            msg.setParams(JsonUtil.toBean(jobInfo.getParamJson(), HashMap.class));

            String destination = subDestination(jobInfo.getDestination(), "rmq://");
            String[] destArr = destination.split(MsgDto.SEPARATOR);
            String tags = TopicGroup.COMMON_GROUP;
            if (destArr.length > 1 && StringUtil.isNotEmpty(destArr[1])) {
                tags = destArr[1];
            }

            msg.setTopic(destArr[0]);
            msg.setTags(tags);
            return rmqSender.sendOne(msg);
        } catch (Throwable e) {
            logger.error("发送RocketMQ消息时出现异常 jobGroup={} jobName={}", jobInfo.getJobGroup(), jobInfo.getJobName(), e);
            return false;
        }
    }

    private boolean sendActiveMQ(JobInfoDto jobInfo){
        if(amqSender == null){
            throw new BizException(BizException.BIZ_INVALID, "无法发送ActiveMQ信息，请检查ActiveMQ相关配置信息");
        }

        try{
            MsgDto msg = new MsgDto();
            msg.setTrxNo(this.buildTrxNo(jobInfo));
            msg.setParams(JsonUtil.toBean(jobInfo.getParamJson(), HashMap.class));

            String destination = subDestination(jobInfo.getDestination(), "amq://");
            String[] destArr = destination.split(MsgDto.SEPARATOR);
            String tags = TopicGroup.COMMON_GROUP;
            if (destArr.length > 1 && StringUtil.isNotEmpty(destArr[1])) {
                tags = destArr[1];
            }

            msg.setTopic(destArr[0]);
            msg.setTags(tags);
            return amqSender.sendOne(msg);
        }catch(Throwable e){
            logger.error("发送ActiveMQ消息时出现异常 jobGroup={} jobName={}", jobInfo.getJobGroup(), jobInfo.getJobName(), e);
            return false;
        }
    }

    private boolean sendRabbitMQ(JobInfoDto jobInfo){
        if (raqSender == null) {
            throw new BizException(BizException.BIZ_INVALID, "无法发送RabbitMQ信息，请检查RabbitMQ相关配置信息");
        }

        try{
            MsgDto msg = new MsgDto();
            msg.setTrxNo(this.buildTrxNo(jobInfo));
            msg.setParams(JsonUtil.toBean(jobInfo.getParamJson(), HashMap.class));

            String destination = subDestination(jobInfo.getDestination(), "raq://");
            String[] destArr = destination.split(MsgDto.SEPARATOR);
            if (destArr.length < 2) {
                throw new BizException(BizException.BIZ_INVALID, "消息目的地需要同时指定Exchange和Queue名称！");
            }

            msg.setTopic(destArr[0]);//设置Exchange名称，默认使用Direct类型的转换器
            msg.setTags(destArr[1]);//设置Queue名称，RoutingKey和Queue名称相同
            return raqSender.sendOne(msg);
        }catch(Throwable e){
            logger.error("发送RabbitMQ消息时出现异常 jobGroup={} jobName={}", jobInfo.getJobGroup(), jobInfo.getJobName(), e);
            return false;
        }
    }

    private boolean sendHttp(JobInfoDto jobInfo){
        try{
            String paramJson = StringUtil.isNotEmpty(jobInfo.getParamJson()) ? jobInfo.getParamJson() : "{}";
            HttpUtil.postJsonAsync(jobInfo.getDestination(), paramJson, new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    logger.error("Http请求发送失败 jobGroup={} jobName={}", jobInfo.getJobGroup(), jobInfo.getJobName(), e);
                }
            });
            return true;
        }catch(Throwable e){
            logger.error("发送http通知时出现异常 jobGroup={} jobName={}", jobInfo.getJobGroup(), jobInfo.getJobName(), e);
            return false;
        }
    }

    private String buildTrxNo(JobInfoDto jobInfo) {
        return jobInfo.getJobGroup() + "-" + jobInfo.getJobName();
    }

    private String subDestination(String destination, String prefix){
        return destination.substring(prefix.length());
    }
}
