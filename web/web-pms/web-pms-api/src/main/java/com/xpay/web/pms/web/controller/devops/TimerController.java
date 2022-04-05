package com.xpay.web.pms.web.controller.devops;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.timer.dto.InstanceDto;
import com.xpay.facade.timer.dto.JobInfoDto;
import com.xpay.facade.timer.dto.OpLogDto;
import com.xpay.facade.timer.enums.TimerStatus;
import com.xpay.facade.timer.service.TimerAdminFacade;
import com.xpay.facade.timer.service.TimerFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.devops.JobInfoVo;
import com.xpay.web.pms.web.vo.devops.JobInfoQueryVo;
import com.xpay.web.pms.web.vo.devops.TimerOpLogVo;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务控制器
 */
@RestController
@RequestMapping("devops")
public class TimerController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    TimerFacade timerFacade;
    @DubboReference
    TimerAdminFacade timerAdminFacade;

    /**
     * 查询定时任务
     * @return
     */
    @Permission("devops:timer:list")
    @RequestMapping("listScheduleJob")
    public RestResult<PageResult<List<JobInfoDto>>> listJobInfo(@RequestBody JobInfoQueryVo queryVo){
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());

        String sortColumns = "ID DESC";
        if(StringUtil.isNotEmpty(queryVo.getSort())){
            if("CREATE_TIME_DESC".equals(queryVo.getSort())){
                sortColumns = "CREATE_TIME DESC";
            }else if("GROUP_ASC".equals(queryVo.getSort())){
                sortColumns = "JOB_GROUP ASC";
            }else if("NAME_ASC".equals(queryVo.getSort())){
                sortColumns = "JOB_NAME ASC";
            }
            paramMap.put("sortColumns", sortColumns);
        }

        PageResult<List<JobInfoDto>> pageResult = timerFacade.listPage(paramMap, pageQuery);
        return RestResult.success(pageResult);
    }

    /**
     * 保存添加定时任务
     * @return
     */
    @Permission("devops:timer:manage")
    @RequestMapping("addJob")
    public RestResult<String> addJob(@RequestBody @Valid JobInfoVo jobVo, @CurrentUser UserModel userModel){
        if(jobVo.getJobType().intValue() == JobInfoDto.INTERVAL_JOB){
            if(jobVo.getIntervals() == null || jobVo.getIntervals() <= 0){
                return RestResult.error("任务间隔需大于0");
            }else if(jobVo.getIntervalUnit() == null || jobVo.getIntervalUnit() <= 0){
                return RestResult.error("任务间隔单位需大于0");
            }
            jobVo.setCronExpression(null);
        }else if(jobVo.getJobType().intValue() == JobInfoDto.CRON_JOB){
            if(StringUtil.isEmpty(jobVo.getCronExpression())){
                return RestResult.error("cron表达式不能为空");
            }
            jobVo.setIntervals(null);
            jobVo.setIntervalUnit(null);
        }else{
            return RestResult.error("未预期的任务类型：" + jobVo.getJobType());
        }

        JobInfoDto jobInfo = BeanUtil.newAndCopy(jobVo, JobInfoDto.class);
        try{
            timerFacade.scheduleJob(jobInfo, userModel.getLoginName());
            return RestResult.success("添加成功");
        }catch(BizException e){
            return RestResult.error("添加失败，" + e.getMsg());
        }
    }

    /**
     * 暂停定时任务
     * @return
     */
    @Permission("devops:timer:operate")
    @RequestMapping("pauseJob")
    public RestResult pauseJob(@RequestParam String jobGroup, @RequestParam String jobName, @CurrentUser UserModel userModel){
        try {
            timerFacade.pauseJob(jobGroup, jobName, userModel.getLoginName());
            return RestResult.success("暂停成功");
        } catch (BizException e) {
            logger.error("定时任务暂停失败", e);
            return RestResult.error("暂停失败，" + e.getMsg());
        }
    }

    /**
     * 恢复被暂停的定时任务
     * @return
     */
    @Permission("devops:timer:operate")
    @RequestMapping("resumeJob")
    public RestResult resumeJob(@RequestParam String jobGroup, @RequestParam String jobName, @CurrentUser UserModel userModel){
        try {
            timerFacade.resumeJob(jobGroup, jobName, userModel.getLoginName());
            return RestResult.success("恢复成功");
        } catch (BizException e) {
            logger.error("定时任务恢复失败", e);
            return RestResult.error("恢复失败，" + e.getMsg());
        }
    }

    /**
     * 删除定时任务
     * @return
     */
    @Permission("devops:timer:manage")
    @RequestMapping("deleteJob")
    public RestResult deleteJob(@RequestParam String jobGroup, @RequestParam String jobName, @CurrentUser UserModel userModel){
        try {
            timerFacade.deleteJob(jobGroup, jobName, userModel.getLoginName());
            return RestResult.success("删除成功");
        } catch (BizException e) {
            logger.error("定时任务删除失败", e);
            return RestResult.error("删除失败，" + e.getMsg());
        }
    }

    /**
     * 保存编辑的定时任务
     * @return
     */
    @Permission("devops:timer:manage")
    @RequestMapping("editJob")
    public RestResult editJob(@RequestBody @Valid JobInfoVo jobVo, @CurrentUser UserModel userModel){
        JobInfoDto jobInfo = timerFacade.getJobInfoByName(jobVo.getJobGroup(), jobVo.getJobName());
        if(jobInfo == null){
            return RestResult.error("任务记录不存在");
        }

        if(jobInfo.getJobType().intValue() == JobInfoDto.INTERVAL_JOB){
            if(jobVo.getIntervals() == null || jobVo.getIntervals() <= 0){
                return RestResult.error("任务间隔需大于0");
            }else if(jobVo.getIntervalUnit() == null || jobVo.getIntervalUnit() <= 0){
                return RestResult.error("任务间隔单位需大于0");
            }
        }else if(jobInfo.getJobType().intValue() == JobInfoDto.CRON_JOB){
            if(StringUtil.isEmpty(jobVo.getCronExpression())){
                return RestResult.error("cron表达式不能为空");
            }
        }else{
            return RestResult.error("未预期的任务类型");
        }

        jobInfo.setDestination(jobVo.getDestination());
        jobInfo.setEndTime(jobVo.getEndTime());
        jobInfo.setJobDescription(jobVo.getJobDescription());
        jobInfo.setParamJson(jobVo.getParamJson());
        if(jobInfo.getJobType().intValue() == JobInfoDto.INTERVAL_JOB){
            jobInfo.setIntervals(jobVo.getIntervals());
            jobInfo.setIntervalUnit(jobVo.getIntervalUnit());
        }else if(jobInfo.getJobType().intValue() == JobInfoDto.CRON_JOB){
            jobInfo.setCronExpression(jobVo.getCronExpression());
        }

        try {
            timerFacade.rescheduleJob(jobInfo, userModel.getLoginName());
            return RestResult.success("编辑成功");
        } catch (BizException e) {
            logger.error("定时任务编辑失败", e);
            return RestResult.error("编辑失败，" + e.getMsg());
        }
    }

    /**
     * 立即触发一次任务
     * @return
     */
    @Permission("devops:timer:operate")
    @RequestMapping("triggerJob")
    public RestResult triggerJob(@RequestParam String jobGroup, @RequestParam String jobName, @CurrentUser UserModel userModel){
        try {
            timerFacade.triggerJob(jobGroup, jobName, userModel.getLoginName());
            return RestResult.success("触发成功");
        } catch (BizException e) {
            logger.error("定时任务触发失败", e);
            return RestResult.error("触发失败，" + e.getMsg());
        }
    }

    @Permission("devops:timer:operate")
    @RequestMapping("notifyJob")
    public RestResult notifyJob(@RequestParam String jobGroup, @RequestParam String jobName, @CurrentUser UserModel userModel){
        try {
            return timerFacade.sendJobNotify(jobGroup, jobName, userModel.getLoginName()) ? RestResult.success("通知成功") : RestResult.error("通知失败");
        } catch (BizException e) {
            logger.error("定时任务通知失败", e);
            return RestResult.error("通知失败，" + e.getMsg());
        }
    }

    @Permission("devops:timer:instanceManage")
    @RequestMapping("listInstance")
    public RestResult<PageResult<List<InstanceDto>>> listInstance(Integer pageSize) {
        if(pageSize == null || pageSize <= 0 || pageSize >= 500){
            pageSize = 500;
        }
        PageQuery pageQuery = PageQuery.newInstance(1, pageSize, "STATUS asc,SCHEDULE_STATUS asc");
        PageResult<List<InstanceDto>> pageResult = timerAdminFacade.listInstancePage(new HashMap<>(), pageQuery);
        return RestResult.success(pageResult);
    }

    @Permission("devops:timer:instanceManage")
    @RequestMapping("adminInstance")
    public RestResult adminInstance(@RequestParam String instanceId, @CurrentUser UserModel userModel) {
        boolean isSuccess = false;
        try{
            InstanceDto instance = timerAdminFacade.getInstanceByInstanceId(instanceId);
            if(instance == null){
                return RestResult.error("当前实例不存在！");
            }
            if (TimerStatus.STAND_BY.getValue() == instance.getScheduleStatus()) {
                isSuccess = timerAdminFacade.resumeInstance(instanceId, userModel.getLoginName());
            } else if (TimerStatus.RUNNING.getValue() == instance.getScheduleStatus()) {
                isSuccess = timerAdminFacade.pauseInstance(instanceId, userModel.getLoginName());
            }
            return isSuccess ? RestResult.success("操作成功") : RestResult.error("操作失败！");
        }catch(BizException e){
            return RestResult.error(e.getMsg());
        }
    }

    @Permission("devops:timer:opLogList")
    @RequestMapping("listTimerOpLog")
    public RestResult<PageResult<List<OpLogDto>>> listTimerOpLog(@RequestBody TimerOpLogVo queryVo) {
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<OpLogDto>> pageResult = timerAdminFacade.listOpLogPage(paramMap, pageQuery);
        return RestResult.success(pageResult);
    }
}
