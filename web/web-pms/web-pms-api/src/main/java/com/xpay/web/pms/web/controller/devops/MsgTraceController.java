package com.xpay.web.pms.web.controller.devops;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.constants.mqdest.VTopicConsume;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.AmqTraceDto;
import com.xpay.facade.message.service.AmqTraceFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.devops.MQTraceQueryVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 消息轨迹控制器
 */
@RestController
@RequestMapping("devops")
public class MsgTraceController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    AmqTraceFacade amqTraceFacade;

    @Permission("devops:mqTrace:list")
    @RequestMapping("listMQTrace")
    public RestResult<PageResult<List<AmqTraceDto>>> listMQTrace(@RequestBody MQTraceQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        if(paramMap.get("msgTimeBegin") == null){
            paramMap.put("msgTimeBegin", DateUtil.formatDateTime(DateUtil.getDayStart(new Date())));
        }
        if(paramMap.get("msgTimeEnd") == null){
            paramMap.put("msgTimeEnd", DateUtil.formatDateTime(DateUtil.getDayEnd(new Date())));
        }
        PageResult<List<AmqTraceDto>> pageResult = amqTraceFacade.listAmqTracePage(paramMap, pageQuery);
        return RestResult.success(pageResult);
    }

    @Permission("devops:mqTrace:list")
    @RequestMapping("getTopicInfo")
    public RestResult<Map> getTopicInfo(){
        Map<String, Object> topicInfoMap = new HashMap<>();
        topicInfoMap.put("groupMap", TopicGroup.toMap());
        topicInfoMap.put("topicMap", TopicDest.toMap());
        topicInfoMap.put("groupTopicMap", TopicDest.toGroupMap());
        topicInfoMap.put("vtopicConsumeMap", VTopicConsume.toVTopicMap());
        return RestResult.success(topicInfoMap);
    }

    @Permission("devops:mqTrace:manage")
    @RequestMapping("resendOriMsg")
    public RestResult<String> resendOriMsg(@RequestParam Long recordId){
        try{
            boolean isOk = amqTraceFacade.resendOriMsg(recordId);
            return isOk ? RestResult.success("发送成功") : RestResult.error("发送失败");
        }catch(BizException e){
            return RestResult.error(e.getMsg());
        }
    }

    @Permission("devops:mqTrace:manage")
    @RequestMapping("resendOriMsgBatch")
    public RestResult<String> resendOriMsgBatch(@RequestParam String recordIdStr){
        int successCount = 0, failCount = 0;
        String[] recordIdArr = recordIdStr.split(",");
        for(int i=0; i<recordIdArr.length; i++){
            if(StringUtil.isEmpty(recordIdArr[i])){
                continue;
            }

            try{
                Long recordId = Long.valueOf(recordIdArr[i]);
                boolean isOk = amqTraceFacade.resendOriMsg(recordId);
                if(isOk){
                    successCount ++;
                }else{
                    failCount ++;
                }
            }catch(BizException e){
                failCount ++;
                logger.error("traceRecordId={} 消息补发失败, errMsg={}", recordIdArr[i], e.getMsg());
            }catch(Exception e){
                failCount ++;
                logger.error("traceRecordId={} 消息补发失败, errMsg={}", recordIdArr[i], e.getMessage());
            }
        }

        return RestResult.success("批量补发完毕，成功(" + successCount + ")条，失败(" + failCount + ")条");
    }

    @Permission("devops:mqTrace:manage")
    @RequestMapping("sendCompensate")
    public RestResult<String> sendCompensate(@RequestBody String msgJsonStrList, @CurrentUser UserModel userModel){
        String operator = userModel.getLoginName();
        if(StringUtil.isEmpty(msgJsonStrList)){
            return RestResult.error("消息内容不能为空");
        }

        int successCount = 0, failCount = 0;
        StringBuilder failStr = new StringBuilder("第");
        String[] msgJsonStrArr = msgJsonStrList.split("\n");
        int totalCount = msgJsonStrArr.length;
        for(int i=0; i<msgJsonStrArr.length; i++){
            try{
                String msgJsonStr = msgJsonStrArr[i].trim();
                if(StringUtil.isEmpty(msgJsonStr)){
                    totalCount--;//减掉总数
                    continue;
                }
                boolean isOk = amqTraceFacade.sendCompensate(msgJsonStr, operator);
                if(isOk){
                    successCount++;
                }else{
                    failCount++;
                    failStr.append(i+1);
                }
            }catch(Throwable e){
                failCount++;
                failStr.append(i+1);
            }
            failStr.append(",");
        }

        if(successCount == totalCount){
            return RestResult.success(successCount+"条消息全部操作成功！");
        }else if(failCount == totalCount){
            return RestResult.error(failCount+"条消息全部操作失败！");
        }else{
            return RestResult.success("成功"+successCount+"条，失败"+failCount+"条，分别是："+failStr.toString()+"条");
        }
    }
}
