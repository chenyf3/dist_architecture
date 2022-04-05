package com.xpay.service.message.biz.mq;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.AmqTraceDto;
import com.xpay.service.message.dao.AmqTraceDao;
import com.xpay.service.message.entity.AmqTrace;
import com.xpay.starter.amq.enhance.Enhancer;
import com.xpay.starter.amq.tracer.TraceMsg;
import com.xpay.starter.plugin.consts.MQHeaders;
import com.xpay.starter.plugin.plugins.MQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AmqTraceBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static int PRODUCE = 1;
    private final static int CONSUME = 2;

    @Autowired
    AmqTraceDao amqTraceDao;
    @Autowired
    @Qualifier("amqSender")
    MQSender amqSender;

    public boolean resendOriMsg(Long recordId){
        if(recordId == null || recordId <= 0){
            return false;
        }

        AmqTrace traceRecord = amqTraceDao.getById(recordId);
        if(traceRecord == null){
            throw new BizException(BizException.BIZ_INVALID, "recordId="+recordId+"的消息轨迹记录不存在！");
        }else if(traceRecord.getResend() != 2){//限制一下补发时不能从补发创建的消息来发送，避免补发机制变得混乱
            throw new BizException(BizException.BIZ_INVALID, "recordId="+recordId+"本身为补发创建的消息，不能用以补发消息！");
        }

        String oriMsg = traceRecord.getOriMsg();
        if(StringUtil.isEmpty(oriMsg)){
            AmqTraceDto amqTrace = amqTraceDao.getOriMsgByTraceId(traceRecord.getTraceId());//根基轨迹id(即消息id)查找源消息体
            if(amqTrace == null){
                throw new BizException(BizException.BIZ_INVALID, "traceId="+traceRecord.getTraceId()+"查询不到生产记录和第一条消费记录");
            }else if(amqTrace.getOriMsg() == null || amqTrace.getOriMsg().trim().length() <= 0){
                throw new BizException(BizException.BIZ_INVALID, "源消息体为null，无法补发");
            }else{
                oriMsg = amqTrace.getOriMsg();
            }
        }

        return this.sendAmqJsonStrMsg(traceRecord.getConsumeDest(), oriMsg, true);
    }

    /**
     * 发送补偿消息
     * @param msgJsonStr
     * @return
     */
    public boolean sendCompensate(String msgJsonStr, String operator){
        boolean isOk = this.sendAmqJsonStrMsg(null, msgJsonStr, false);
        if(isOk){
            logger.info("补偿消息发送成功 operator={} msgJsonStr={}", operator, msgJsonStr);
        }else{
            logger.error("补偿消息发送失败 operator={} msgJsonStr={}", operator, msgJsonStr);
        }
        return isOk;
    }

    public PageResult<List<AmqTraceDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<AmqTrace>> result = amqTraceDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AmqTraceDto.class), result);
    }

    public AmqTraceDto getAmqTraceById(Long id){
        if(id == null){
            return null;
        }else{
            AmqTrace amqTrace = amqTraceDao.getById(id);
            if(amqTrace.getOriMsg() == null){
                AmqTraceDto trace2 = amqTraceDao.getOriMsgByTraceId(amqTrace.getTraceId());
                if(trace2 != null){
                    amqTrace.setOriMsg(trace2.getOriMsg());
                }
            }
            return BeanUtil.newAndCopy(amqTrace, AmqTraceDto.class);
        }
    }

    public List<AmqTraceDto> listAmqTraceByTraceId(Long traceId){
        if(traceId == null){
            return null;
        }else{
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("traceId", traceId);
            List<AmqTrace> amqTraces = amqTraceDao.listBy(paramMap);
            return BeanUtil.newAndCopy(amqTraces, AmqTraceDto.class);
        }
    }

    public List<AmqTraceDto> listAmqTraceByTrxNo(Long trxNo){
        if(trxNo == null){
            return null;
        }else{
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("trxNo", trxNo);
            List<AmqTrace> amqTraces = amqTraceDao.listBy(paramMap);
            return BeanUtil.newAndCopy(amqTraces, AmqTraceDto.class);
        }
    }

    public boolean handleAmqTraceMsg(TraceMsg traceMsg){
        if(Enhancer.Type.PRODUCE.name().equals(traceMsg.getType())){
            return addTraceMsg(traceMsg);
        }else if(Enhancer.Type.CONSUME.name().equals(traceMsg.getType())){
            String traceId = subTraceId(traceMsg.getTraceId());
            String clientFlag = subClientFlag(traceMsg.getClient());
            if(StringUtil.isEmpty(traceId) || StringUtil.isEmpty(clientFlag) || traceMsg.getDeliveryCount() == 1){
                return addTraceMsg(traceMsg);
            }else{
                //先查询第1次消费的记录是否存在，如果不存在，则查询最后消费的那条记录
                AmqTrace amqTrace = amqTraceDao.getFirstConsumeRecordByTraceIdAndClientFlag(traceId, clientFlag);
                if(amqTrace == null){
                    amqTrace = amqTraceDao.getLastConsumeRecordByTraceIdAndClientFlag(traceId, clientFlag);
                }
                if(amqTrace == null){
                    return addTraceMsg(traceMsg);
                }

                //如果消费记录已存在，则更新记录状态和错误信息等
                if(amqTrace.getMsgStatus() == 2){ //如果前面已经成功过，则不更新数据了
                    amqTrace.setDeliveryCount(traceMsg.getDeliveryCount());
                    amqTrace.setMsgStatus(traceMsg.getStatus());
                    if(StringUtil.isNotEmpty(traceMsg.getErrMsg())){
                        amqTrace.setErrMsg(traceMsg.getErrMsg());
                    }
                    amqTraceDao.update(amqTrace);
                }
                return true;
            }
        }else{
            throw new BizException(BizException.BIZ_INVALID, "未识别的消息轨迹类型 type:" + traceMsg.getType());
        }
    }

    private boolean addTraceMsg(TraceMsg traceMsg){
        String traceId = subTraceId(traceMsg.getTraceId());
        String clientFlag = subClientFlag(traceMsg.getClient());
        String trxNo = StringUtil.subLeft(traceMsg.getTrxNo(), 32);
        String mchNo = StringUtil.subLeft(traceMsg.getMchNo(), 16);
        String topic = "", group = "";
        if(traceMsg.getDestination() != null){
            String[] destArr = traceMsg.getDestination().split(":");
            if(destArr.length >= 2){
                topic = StringUtil.subLeft(destArr[0], 64);
                group = StringUtil.subLeft(destArr[1], 64);
            }else if(destArr.length == 1){
                topic = StringUtil.subLeft(destArr[0], 64);
            }
        }

        AmqTrace amqTrace = new AmqTrace();
        amqTrace.setCreateTime(new Date());
        amqTrace.setMsgTime(traceMsg.getMsgTime());
        amqTrace.setTraceId(traceId != null ? traceId : "");
        amqTrace.setTrxNo(trxNo != null ? trxNo : "");
        amqTrace.setMchNo(mchNo != null ? mchNo : "");
        amqTrace.setTopic(topic != null ? topic : "");
        amqTrace.setTopicGroup(group != null ? group : "");
        //在P2P队列中consumeDest等于topic，在VirtualTopic中，consumeDest不等于topic
        amqTrace.setConsumeDest(traceMsg.getConsumeDest()==null ? "" : StringUtil.subLeft(traceMsg.getConsumeDest(), 80));
        amqTrace.setType(Enhancer.Type.PRODUCE.name().equals(traceMsg.getType()) ? PRODUCE : (Enhancer.Type.CONSUME.name().equals(traceMsg.getType()) ? CONSUME : 0));
        amqTrace.setMsgStatus(traceMsg.getStatus());
        amqTrace.setDeliveryCount(traceMsg.getDeliveryCount());
        amqTrace.setClientFlag(clientFlag);
        amqTrace.setResend(traceMsg.getResend() ? 1 : 2);
        amqTrace.setErrMsg(traceMsg.getErrMsg()==null ? "" : StringUtil.subLeft(traceMsg.getErrMsg(), 256));
        amqTrace.setOriMsg(traceMsg.getOriMsg());
        
        try{
            amqTraceDao.insert(amqTrace);
        }catch(Exception e){
            if(e.getMessage() != null && e.getMessage().contains("Invalid JSON text")){
                logger.error("topic={} trxNo={} mchNo={} traceId={} 的消息为非JSON格式，将不记录消息轨迹", topic, trxNo, mchNo, traceId);
            }else{
                throw e;
            }
        }
        return true;
    }

    /**
     * @param destination
     * @param msgJsonStr
     * @return
     */
    private boolean sendAmqJsonStrMsg(String destination, String msgJsonStr, boolean isResend){
        Map<String, String> oriMsgMap = JsonUtil.toBean(msgJsonStr, HashMap.class); //消息体有可能是MsgDto的子类，所以在这里不能转成MsgDto
        String topic = oriMsgMap.get("topic");
        String group = oriMsgMap.get("tags");
        String trxNo = oriMsgMap.get("trxNo");
        String mchNo = oriMsgMap.get("mchNo");
        if(StringUtil.isEmpty(topic)){
            throw new BizException(BizException.BIZ_INVALID, "源消息体中无法获取消息目的地topic");
        }else if(StringUtil.isEmpty(trxNo)){
            throw new BizException(BizException.BIZ_INVALID, "源消息体中无法获取trxNo");
        }
        if(StringUtil.isEmpty(group)){
            group = TopicDest.getGroup(topic);//尝试去获取topic分组
        }

        //设置消息轨迹需要用到的消息头
        Map<String, String> header = new HashMap<>();
        header.put(MQHeaders.TRACE_TRX_NO, trxNo);
        header.put(MQHeaders.TRACE_MCH_NO, mchNo);
        header.put(MQHeaders.TRACE_RESEND, String.valueOf(isResend));
        header.put(MQHeaders.DESTINATION, StringUtil.isEmpty(group) ? topic : topic + ":" + group);
        if(StringUtil.isNotEmpty(destination)){
            topic = destination;
        }

        return amqSender.sendOne(topic, msgJsonStr, header);
    }

    private String subTraceId(String traceId){
        return StringUtil.subLeft(traceId, 32);
    }
    private String subClientFlag(String clientFlag){
        return clientFlag==null ? "" : StringUtil.subLeft(clientFlag, 100);
    }
}
