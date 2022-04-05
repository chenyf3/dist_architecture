package com.xpay.service.timer.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.service.timer.handler.SchedulerHandler;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import com.xpay.facade.timer.dto.JobInfoDto;
import com.xpay.facade.timer.service.TimerFacade;

import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/20.
 */
@DubboService
public class TimerFacadeImpl implements TimerFacade {
    @Autowired
    SchedulerHandler schedulerHandler;

    @Override
    public void scheduleJob(JobInfoDto jobInfo, String remark){
        schedulerHandler.scheduleJob(jobInfo, remark);
    }

    @Override
    public void rescheduleJob(JobInfoDto jobInfo, String remark){
        schedulerHandler.rescheduleJob(jobInfo, remark);
    }

    @Override
    public void deleteJob(String jobGroup, String jobName, String remark){
        schedulerHandler.deleteJob(jobGroup, jobName, remark);
    }

    @Override
    public void pauseJob(String jobGroup, String jobName, String remark){
        schedulerHandler.pauseJob(jobGroup, jobName, remark);
    }

    @Override
    public void resumeJob(String jobGroup, String jobName, String remark){
        schedulerHandler.resumeJob(jobGroup, jobName, remark);
    }

    @Override
    public void triggerJob(String jobGroup, String jobName, String remark){
        schedulerHandler.triggerJob(jobGroup, jobName, remark);
    }

    /**
     * 直接触发任务的消息通知，实例处于挂起状态中，但需要对个别任务触发时使用（实行 蓝绿发布 策略时可能会有用）
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean sendJobNotify(String jobGroup, String jobName, String remark){
        return schedulerHandler.sendJobNotify(jobGroup, jobName, remark);
    }

    @Override
    public JobInfoDto getJobInfoByName(String jobGroup, String jobName){
        return schedulerHandler.getJobInfoByName(jobGroup, jobName);
    }

    @Override
    public PageResult<List<JobInfoDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) throws BizException {
        return schedulerHandler.listPage(paramMap, pageQuery);
    }
}
