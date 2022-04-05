package com.xpay.service.timer.biz;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.service.timer.dao.JobInfoDao;
import com.xpay.service.timer.entity.JobInfo;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xpay.facade.timer.dto.JobInfoDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ext_job_info表的逻辑处理层，这个表等同于Quartz中的 JobDetail + Trigger，通过这一张表，可以更方便的检索、管理Quartz中的任务，在Quartz中
 * 任何关于 JobDetail和Trigger的操作，都会同步到ext_job_info表中
 *  注：极端情况下(如：数据库连接池不足、网络不好等)可能会出现Quartz中修改成功但是同步到ext_job_info表时失败的情况，此时可人工介入处理
 * @author chenyf on 2020/5/26.
 */
@Component
public class ExtJobInfoBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    JobInfoDao jobInfoDao;
    @Autowired
    NotifyBiz notifyBiz;

    /**
     * 直接触发任务的消息通知，实例处于挂起状态中，但需要对个别任务触发时使用（实行 蓝绿发布 策略时可能会有用）
     *
     * @param jobDataMap
     * @return
     */
    public boolean sendJobNotify(Map<String, Object> jobDataMap) {
        if(jobDataMap == null || jobDataMap.isEmpty()){
            throw new BizException("jobDataMap不能为空");
        }

        JobInfoDto jobInfo = JsonUtil.toBean(JsonUtil.toJson(jobDataMap), JobInfoDto.class);
        if(jobInfo == null){
            throw new BizException("jobDataMap无法转换为JobInfo, jobDataMap = {}" + JsonUtil.toJson(jobDataMap));
        }

        boolean isOk = notifyBiz.jobNotify(jobInfo);
        if (isOk) {
            logger.info("任务通知完成 jobGroup={} jobName={}", jobInfo.getJobGroup(), jobInfo.getJobName());
        }
        return isOk;
    }

    public void addOrUpdateExtJob(Map<String, Object> jobDataMap) {
        String jobGroup = (String) jobDataMap.get("jobGroup");
        String jobName = (String) jobDataMap.get("jobName");

        JobInfo jobInfoTemp = jobInfoDao.getByName(jobGroup, jobName);
        if(jobInfoTemp == null){
            addJobInfo(jobDataMap);
        }else{
            updateJobInfo(jobDataMap);
        }
    }

    /**
     * 根据 组名+任务名 暂停定时任务
     *
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean pauseExtJob(String jobGroup, String jobName) {
        JobInfo jobInfo = jobInfoDao.getByName(jobGroup, jobName);
        if (jobInfo == null) {
            throw new BizException(BizException.BIZ_INVALID, "任务不存在");
        }

        try {
            jobInfo.setJobStatus(Trigger.TriggerState.PAUSED.name());
            jobInfoDao.update(jobInfo);
            return true;
        } catch (BizException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("暂停任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException(BizException.UNEXPECT_ERROR, "暂停任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 恢复被暂停的定时任务
     *
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean resumeExtJob(String jobGroup, String jobName) {
        JobInfo jobInfo = jobInfoDao.getByName(jobGroup, jobName);
        if (jobInfo == null) {
            throw new BizException(BizException.BIZ_INVALID, "任务不存在");
        }

        try {
            jobInfo.setJobStatus(Trigger.TriggerState.NORMAL.name());
            jobInfoDao.update(jobInfo);
            return true;
        } catch (BizException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("恢复任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException(BizException.UNEXPECT_ERROR, "恢复任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 删除定时任务
     *
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void deleteExtJob(String jobGroup, String jobName) {
        try {
            JobInfo jobInfo = jobInfoDao.getByName(jobGroup, jobName);
            if (jobInfo != null) {
                jobInfoDao.deleteById(jobInfo.getId());
            }
        } catch (BizException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("删除任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException(BizException.UNEXPECT_ERROR, "删除任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 取得定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public JobInfo getJobInfoByName(String jobGroup, String jobName) {
        return jobInfoDao.getByName(jobGroup, jobName);
    }

    /**
     *
     * @param id
     * @return
     */
    public JobInfo getJobInfoById(long id) {
        return jobInfoDao.getById(id);
    }

    /**
     * job被触发并执行完毕之后调用的方法，主要用来同步ScheduleJob一些属性，如：jobStatus、lastExecuteTime、nextExecuteTime、executedTimes等等
     *
     * @param jobGroup
     * @param jobName
     * @param jobProperties
     * @return
     */
    public boolean updatePropertiesAfterExecuted(String jobGroup, String jobName, Map<String, Object> jobProperties) {
        if (jobProperties == null) {
            jobProperties = new HashMap<>(2);
        }
        jobProperties.put("jobGroup", jobGroup);
        jobProperties.put("jobName", jobName);
        return jobInfoDao.update("updateJobInfoAfterExecuted", jobProperties) > 0;
    }

    /**
     * job在被检测到misfire之后调用的方法，主要用来同步ScheduleJob一些属性，如：jobStatus、lastExecuteTime、nextExecuteTime等等
     *
     * @param jobGroup
     * @param jobName
     * @param jobProperties
     * @return
     */
    public boolean updatePropertiesAfterMisfire(String jobGroup, String jobName, Map<String, Object> jobProperties) {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, Object>(2);
        }
        jobProperties.put("jobGroup", jobGroup);
        jobProperties.put("jobName", jobName);
        return jobInfoDao.update("updateJobInfoAfterMisfire", jobProperties) > 0;
    }

    /**
     * 分页查询JobInfo
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public PageResult<List<JobInfoDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<JobInfo>> result = jobInfoDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), JobInfoDto.class), result);
    }

    /**
     * 添加一个定时任务
     * @param jobDataMap
     * @return
     */
    private Long addJobInfo(Map<String, Object> jobDataMap) {
        try {
            JobInfo jobInfo = JsonUtil.toBean(JsonUtil.toJson(jobDataMap), JobInfo.class);
            if(jobInfo.getExecutedTimes() == null){
                jobInfo.setExecutedTimes(0L);
            }
            jobInfoDao.insert(jobInfo);
            return jobInfo.getId();
        } catch (BizException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("添加任务时出现异常 jobDataMap = {} ", JsonUtil.toJson(jobDataMap), e);
            throw new BizException(BizException.UNEXPECT_ERROR, "添加任务发生异常");
        }
    }

    /**
     * 重新安排定时任务的定时规则
     *
     * @param jobDataMap
     * @return
     */
    private void updateJobInfo(Map<String, Object> jobDataMap) {
        JobInfo jobInfo = JsonUtil.toBean(JsonUtil.toJson(jobDataMap), JobInfo.class);
        JobInfo jobInfoTemp = jobInfoDao.getByName(jobInfo.getJobGroup(), jobInfo.getJobName());
        if (jobInfoTemp == null) {
            throw new BizException(BizException.PARAM_INVALID, "任务不存在");
        }

        //以下是不允许修改的字段
        jobInfo.setId(jobInfoTemp.getId());
        jobInfo.setVersion(jobInfoTemp.getVersion());
        jobInfo.setCreateTime(jobInfoTemp.getCreateTime());
        jobInfo.setJobGroup(jobInfoTemp.getJobGroup());
        jobInfo.setJobName(jobInfoTemp.getJobName());
        jobInfo.setJobStatus(jobInfoTemp.getJobStatus());
        jobInfo.setJobType(jobInfoTemp.getJobType());
        jobInfo.setStartTime(jobInfoTemp.getStartTime());
        jobInfo.setLastExecuteTime(jobInfoTemp.getLastExecuteTime());
        if(jobInfo.getNextExecuteTime() == null){
            jobInfo.setNextExecuteTime(jobInfoTemp.getNextExecuteTime());
        }
        jobInfo.setExecutedTimes(jobInfoTemp.getExecutedTimes());
        
        try {
            jobInfoDao.update(jobInfo);
        } catch (BizException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("重新安排任务时出现异常 jobInfo = {} ", JsonUtil.toJson(jobInfo), e);
            throw new BizException(BizException.UNEXPECT_ERROR, "重新安排任务时发生异常", e);
        }
    }
}
