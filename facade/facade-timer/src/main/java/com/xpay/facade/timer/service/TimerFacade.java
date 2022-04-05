package com.xpay.facade.timer.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.timer.dto.JobInfoDto;

import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/20.
 */
public interface TimerFacade {

    /**
     * 添加任务
     * @param jobInfo
     * @return
     */
    public void scheduleJob(JobInfoDto jobInfo, String remark) throws BizException;

    /**
     * 重新安排定时任务，即update任务
     * @param jobInfo
     * @return
     */
    public void rescheduleJob(JobInfoDto jobInfo, String remark) throws BizException;

    /**
     * 删除任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void deleteJob(String jobGroup, String jobName, String remark) throws BizException;

    /**
     * 暂停任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void pauseJob(String jobGroup, String jobName, String remark) throws BizException;

    /**
     * 恢复被暂停的任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void resumeJob(String jobGroup, String jobName, String remark) throws BizException;

    /**
     * 立即触发任务，若实例处于挂起状态，则操作无效
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void triggerJob(String jobGroup, String jobName, String remark) throws BizException;

    /**
     * 直接触发任务的消息通知，实例处于挂起状态中，但需要对个别任务触发时使用（实行 蓝绿发布 策略时可能会有用）
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean sendJobNotify(String jobGroup, String jobName, String remark)  throws BizException;

    public JobInfoDto getJobInfoByName(String jobGroup, String jobName)  throws BizException;

    public PageResult<List<JobInfoDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery)  throws BizException;
}
