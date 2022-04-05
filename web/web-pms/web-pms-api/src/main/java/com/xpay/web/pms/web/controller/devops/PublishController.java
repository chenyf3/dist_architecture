package com.xpay.web.pms.web.controller.devops;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.extend.dto.PublishRecordDto;
import com.xpay.facade.extend.service.DevopsFacade;
import com.xpay.facade.extend.vo.IdcVo;
import com.xpay.facade.extend.vo.PublishInfoVo;
import com.xpay.facade.timer.dto.InstanceDto;
import com.xpay.facade.timer.enums.TimerStatus;
import com.xpay.facade.timer.service.TimerAdminFacade;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.config.WebApiProperties;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.config.AppConstant;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.devops.PublishQueryVo;
import com.xpay.web.pms.web.vo.devops.PublishVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.*;

@RestController
@RequestMapping("devops")
public class PublishController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    WebApiProperties webApiProperties;
    @Autowired
    RedisClient redisClient;

    @DubboReference
    DevopsFacade devopsFacade;
    @DubboReference
    TimerAdminFacade timerAdminFacade;

    @Permission("devops:publish:view")
    @RequestMapping("publishRecordList")
    public RestResult<PageResult<List<PublishRecordDto>>> publishRecordList(@RequestBody PublishQueryVo queryVo){
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        PageResult<List<PublishRecordDto>> pageResult = devopsFacade.listPage(paramMap, pageQuery);
        return RestResult.success(pageResult);
    }

    @Permission("devops:publish:manage")
    @RequestMapping("publish")
    public RestResult<String> publish(@Validated @RequestBody PublishVo publishVo, @CurrentUser UserModel userModel){
        PublishRecordDto publishRecord = BeanUtil.newAndCopy(publishVo, PublishRecordDto.class);
        publishRecord.setCreator(userModel.getLoginName());
        publishRecord.setModifier(userModel.getLoginName());

        try{
            int failCount = 0;
            if(StringUtil.isNotEmpty(publishRecord.getIdc())){
                logAdd("发布项目 buildMsg="+publishRecord.getBuildMsg()+",idc="+publishRecord.getIdc(), userModel);
                failCount = operateTimerInstance(1, publishVo.getIdc(), "["+userModel.getLoginName()+"]项目发布,挂起实例");
            }

            if(failCount == 0){
                String callbackUrl = getBuildNotifyUrl();
                publishRecord.setNotifyUrl(callbackUrl);
                boolean isSuccess = devopsFacade.addPublish(publishRecord, publishVo.getBuildType());
                return isSuccess ? RestResult.success("项目发布提交成功！") : RestResult.warn("定时任务已挂起，但项目发布失败！");
            }else{
                return RestResult.warn("有"+failCount+"个定时任务实例挂起失败，项目未发布");
            }
        }catch(BizException e){
            return RestResult.error(e.getMsg());
        }
    }

    @Permission("devops:publish:manage")
    @RequestMapping("republish")
    public RestResult<String> republish(@RequestParam Long id, @Nullable String relayApp, @Nullable String remark, @CurrentUser UserModel userModel){
        PublishRecordDto publishRecord = devopsFacade.getById(id);
        if(publishRecord == null){
            return RestResult.error("记录不存在!");
        }

        logEdit("重新发布项目 publishId="+id+",buildMsg="+publishRecord.getBuildMsg()+",idc="+publishRecord.getIdc(), userModel);
        int failCount = operateTimerInstance(1, publishRecord.getIdc(), "["+userModel.getLoginName()+"]重新发布项目,挂起实例");

        if(failCount == 0){
            String notifyUrl = getBuildNotifyUrl();
            boolean isSuccess = devopsFacade.republish(id, relayApp, remark, notifyUrl, userModel.getLoginName());
            return isSuccess ? RestResult.success("项目重新发布提交成功！") : RestResult.warn("定时任务已挂起，但项目重新发布失败！");
        }else{
            return RestResult.warn("有"+failCount+"个定时任务实例挂起失败，项目未重新发布");
        }
    }

    @Permission("devops:publish:manage")
    @RequestMapping("cancelPublish")
    public RestResult<String> cancelPublish(@RequestParam Long id, @CurrentUser UserModel userModel){
        boolean isSuccess = devopsFacade.cancelPublish(id, userModel.getLoginName());
        if(isSuccess){
            logEdit("取消任务发布 publishId="+id, userModel);
        }
        return isSuccess ? RestResult.success("取消成功！") : RestResult.warn("取消失败");
    }

    @Permission("devops:publish:manage")
    @RequestMapping("audit")
    public RestResult<String> audit(@RequestParam Long id, @RequestParam Integer auditStatus, @Nullable String remark, @CurrentUser UserModel userModel){
        PublishRecordDto publishRecord = devopsFacade.getById(id);
        if(publishRecord == null){
            return RestResult.error("记录不存在!");
        }
        boolean isSuccess = devopsFacade.auditPublishRecord(id, auditStatus, remark, userModel.getLoginName());
        if(isSuccess){
            logEdit("审核发布项目 publishId="+id+",auditStatus="+auditStatus, userModel);
        }
        return isSuccess ? RestResult.success("审核成功！") : RestResult.warn("审核失败");
    }

    /**
     * 查询机房列表、发布后邮件通知人等
     * @return
     */
    @Permission("devops:publish:manage | devops:publish:flowSwitch | devops:publish:syncIdcPublish")
    @RequestMapping("getPublishInfo")
    public RestResult<PublishInfoVo> getPublishInfo(){
        PublishInfoVo infoVo = devopsFacade.getPublishInfo();
        if(infoVo.getEmailReceiver() != null){//把逗号分割符变成 回车 分隔符
            infoVo.setEmailReceiver(infoVo.getEmailReceiver().replace(",", "\n"));
        }
        return RestResult.success(infoVo);
    }

    /**
     * 查询当前流量在哪个机房
     * @return
     */
    @Permission("devops:publish:manage | devops:publish:flowSwitch | devops:publish:syncIdcPublish")//只要满足一个权限即可访问
    @RequestMapping("getCurrIdcFlow")
    public RestResult<IdcVo> getCurrIdcFlow(){
        IdcVo idcVo = devopsFacade.getCurrIdcFlow();
        return RestResult.success(idcVo);
    }

    /**
     * 执行流量切换
     * @param toIdcs    流量切入的机房，多个机房用英文的逗号分割
     * @param userModel
     * @return
     */
    @Permission("devops:publish:flowSwitch")
    @RequestMapping("flowSwitch")
    public RestResult<String> flowSwitch(@RequestParam String toIdcs, @RequestParam Boolean checkPublishing, @CurrentUser UserModel userModel){
        if(StringUtil.isEmpty(toIdcs)){
            return RestResult.error("切入机房不能为空");
        }

        List<String> toIdcList = Arrays.asList(toIdcs.split(","));
        boolean isSuccess = devopsFacade.flowSwitch(toIdcList, checkPublishing, userModel.getLoginName());
        if(isSuccess){
            logEdit("机房流量切入[toIdcs="+toIdcs+"]", userModel);
            return RestResult.success("操作成功!");
        }else{
            return RestResult.error("操作失败");
        }
    }

    /**
     * 同步机房发布
     * @param userModel
     * @return
     */
    @Permission("devops:publish:syncIdcPublish")
    @RequestMapping("syncIdcPublish")
    public RestResult<String> syncIdcPublish(@RequestParam String toIdc, @RequestParam String publishIds,
                                             @Nullable String syncMsg, @Nullable String timerResumeIdc,
                                             @CurrentUser UserModel userModel){
        List<Long> publishIdList = new ArrayList<>();
        if(StringUtil.isNotEmpty(publishIds)){
            String[] idArr = publishIds.split(",");
            for(String idStr : idArr){
                publishIdList.add(Long.valueOf(idStr));
            }
        }

        String notifyUrl = getSyncNotifyUrl();
        boolean isSuccess = devopsFacade.syncIdcPublish(toIdc, publishIdList, syncMsg, notifyUrl, userModel.getLoginName());
        int failCount = 0;
        if(isSuccess && StringUtil.isNotEmpty(timerResumeIdc)){
            failCount = operateTimerInstance(2, timerResumeIdc, syncMsg);
        }
        if(isSuccess){
            logEdit("项目机房同步[toIdc="+toIdc+"]", userModel);
            return failCount == 0 ? RestResult.success("操作成功!") : RestResult.warn("项目同步成功,但"+failCount+"个定时任务实例恢复失败");
        }else{
            return RestResult.error("操作失败");
        }
    }

    /**
     * 挂起机房的所有timer实例
     * @param type      操作类型 1=挂起 2=恢复
     * @param toIdc
     * @param remark
     */
    private int operateTimerInstance(int type, String toIdc, String remark){
        int pauseType = 1;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("scheduleStatus", type == pauseType ? TimerStatus.RUNNING.getValue() : TimerStatus.STAND_BY.getValue());
        PageResult<List<InstanceDto>> pageResult = timerAdminFacade.listInstancePage(paramMap, PageQuery.newInstance(1, 500));
        List<String> instanceIdList = new ArrayList<>();
        for(InstanceDto instance : pageResult.getData()){
            String hostname = instance.getHost();
            boolean isMatch = devopsFacade.isInSameIdc(toIdc, hostname);
            if(isMatch){
                instanceIdList.add(instance.getInstanceId());
            }
        }

        int failCount = 0;
        for(String instanceId : instanceIdList){
            try{
                if(type == pauseType){
                    timerAdminFacade.pauseInstance(instanceId, remark);
                }else{
                    timerAdminFacade.resumeInstance(instanceId, remark);
                }
            }catch (Exception e){
                failCount ++;
                logger.error("操作定时任务实例时异常 type={} instanceId={}", type, instanceId, e);
            }
        }
        return failCount;
    }

    /**
     * 获取构建完毕之后的通知回调地址
     * @return
     */
    private String getBuildNotifyUrl(){
        String host = webApiProperties.getPlatformUrl();
        String path = "/public/publishCallback";
        String token = RandomUtil.get32LenStr();
        String cacheKey = AppConstant.PROJECT_PUBLISH_TOKEN_KEY + ":" + token;
        redisClient.set(cacheKey, "1", 60 * 60);
        return host + path + "?token=" + token;
    }
    /**
     * 获取同步完成之后的回调地址
     * @return
     */
    private String getSyncNotifyUrl(){
        String host = webApiProperties.getPlatformUrl();
        String path = "/public/syncCallback";
        String token = RandomUtil.get32LenStr();
        String cacheKey = AppConstant.PROJECT_PUBLISH_TOKEN_KEY + ":" + token;
        redisClient.set(cacheKey, "1", 60 * 10);
        return host + path + "?token=" + token;
    }
}