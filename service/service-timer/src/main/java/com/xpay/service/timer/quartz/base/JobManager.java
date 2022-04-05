package com.xpay.service.timer.quartz.base;

import com.xpay.common.statics.enums.common.TimeUnitEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import org.quartz.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import com.xpay.facade.timer.dto.JobInfoDto;

import java.util.Date;
import java.util.Map;

/**
 * Quartz任务管理器
 * 说明：
 *  1、定时任务其实分为两个东西：trigger、jobDetail，其中trigger描述何时触发、怎样触发，jobDetail则描述这是一个什么样的job，触发后执行哪个类等
 *  2、Quartz本身的设计是一个jobDetail可以有多个trigger，而一个trigger只能有一个jobDetail，但是为了简单，在此类中的方法都是设计为一对一关系，
 *  即一个trigger只有一个jobDetail，一个jobDetail也只有一个trigger，添加任务时会同时添加trigger和jobDetail，修改时也会同时修改，删除时
 *  也会同时删除，并且两者的group、name是一样的
 * @author chenyf on 2020/3/10
 */
public class JobManager {
    /**
     * 这个SchedulerFactoryBean是spring整合Quartz的对象，通过这个对象来对Quartz进行操作
     */
    private SchedulerFactoryBean schedulerFactoryBean;

    public JobManager(SchedulerFactoryBean schedulerFactoryBean){
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    /**
     * 新增任务（会把jobInfo保存到Trigger里面，然后同步到ext_job_info表）
     * @param jobInfo
     * @return      返回这个任务下次触发的时间
     */
    public Date scheduleJob(JobInfoDto jobInfo) throws BizException {
        if(checkJobExist(jobInfo)){ //任务已存在，则直接更新
            return rescheduleJob(jobInfo);
        }

        checkJobParamAndSetDefaultValue(jobInfo);
        JobDetail jobDetail = JobBuilder
                .newJob(JobImpl.class)
                .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
                //当没有trigger关联的时候是否保留jobDetail，为false表示不保留，因为我们设计成JobDetail和Trigger是一对一关系，所以如果Trigger没有了，JobDetail也没必要保留了
                .storeDurably(false)
                .build();

        Trigger trigger = genTrigger(jobInfo);

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try{
            return scheduler.scheduleJob(jobDetail, trigger);
        }catch(Exception e){
            throw new BizException("新增任务异常", e);
        }
    }

    /**
     * 更新任务(实际上是更新的该任务关联的trigger)
     * @param jobInfo
     * @return
     */
    public Date rescheduleJob(JobInfoDto jobInfo) throws BizException {
        checkJobParamAndSetDefaultValue(jobInfo);

        if(! checkJobExist(jobInfo)){
            throw new BizException(BizException.BIZ_INVALID, "任务不在定时计划中，无法重新安排");
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobName(), jobInfo.getJobGroup());

        //获取trigger
        Trigger trigger = genTrigger(jobInfo);
        //按新的Trigger重新设置job执行
        try{
            return scheduler.rescheduleJob(triggerKey, trigger);
        }catch(Exception e){
            throw new BizException("修改任务任务触发规则异常", e);
        }
    }

    /**
     * 暂停任务(实际上是暂停这个任务关联的触发器，因为我们设计成了任务和触发器是一对一关系)
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void pauseJob(String jobGroup, String jobName) throws BizException {
        JobInfoDto jobInfo = new JobInfoDto(jobGroup, jobName);
        if(! checkJobExist(jobInfo)){
            throw new BizException(BizException.BIZ_INVALID, "任务不在定时计划中，无法暂停");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobName(), jobInfo.getJobGroup());
        try{
            scheduler.pauseTrigger(triggerKey);//暂停这个任务关联的 Trigger 即可，因为我们现在规定好 Trigger 和 Job 一对一关系
        }catch(Exception e){
            throw new BizException("暂停任务异常", e);
        }
    }

    /**
     * 恢复被暂停的任务(实际上恢复这个任务关联的触发器，因为我们设计成了任务和触发器是一对一关系)
     * @param jobGroup
     * @param jobName
     * @return      返回任务状态
     */
    public void resumeJob(String jobGroup, String jobName) throws BizException {
        JobInfoDto jobInfo = new JobInfoDto(jobGroup, jobName);
        if(! checkJobExist(jobInfo)){
            throw new BizException(BizException.BIZ_INVALID, "任务不在定时计划中，无法恢复");
        }

        TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobName(), jobInfo.getJobGroup());
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try{
            scheduler.resumeTrigger(triggerKey);//恢复这个任务关联的 Trigger 即可，因为我们现在规定好 Trigger 和 Job 一对一关系
        }catch(Exception e){
            throw new BizException("恢复任务异常", e);
        }
    }

    /**
     * 触发任务，实现方式是为这个Job关联一个立即执行且只执行一次的Trigger(触发器名称包含时间戳和随机值，不会影响到原来已关联的那个Trigger)
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void triggerJob(String jobGroup, String jobName) throws BizException {
        JobInfoDto jobInfo = new JobInfoDto(jobGroup, jobName);
        if(! checkJobExist(jobInfo)){
            throw new BizException(BizException.BIZ_INVALID, "任务不在定时计划中，无法执行");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobInfo.getJobName(), jobInfo.getJobGroup());
        try{
            Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(jobName, jobGroup));
            scheduler.triggerJob(jobKey, trigger.getJobDataMap());
        }catch(Exception e){
            throw new BizException("触发任务异常", e);
        }
    }

    /**
     * 删除任务(会一并删除其关联的触发器)，如果任务已不存在，则直接返回success
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean deleteJob(String jobGroup, String jobName) throws BizException {
        JobInfoDto jobInfo = new JobInfoDto(jobGroup, jobName);
        if(! checkJobExist(jobInfo)){
            return true;
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobInfo.getJobName(), jobInfo.getJobGroup());
        try {
            return scheduler.deleteJob(jobKey);
        } catch(Exception e) {
            throw new BizException("删除任务异常", e);
        }
    }

    /**
     * 获取Job中的参数，实际是从Job关联的Trigger上获取的
     * @param jobGroup
     * @param jobName
     * @return
     */
    public Map<String, Object> getJobDataMap(String jobGroup, String jobName){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        try{
            return scheduler.getTrigger(triggerKey).getJobDataMap().getWrappedMap();
        }catch(Exception e){
            throw new BizException("获取任务数据时出现异常", e);
        }
    }

    /**
     * 根据jobGroup、jobName检查一个任务是否已经存在
     * @param jobInfo
     * @return
     */
    private boolean checkJobExist(JobInfoDto jobInfo) throws BizException {
        if(jobInfo == null || StringUtil.isEmpty(jobInfo.getJobGroup()) || StringUtil.isEmpty(jobInfo.getJobName())){
            throw new BizException("jobGroup和jobName不能为空");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        //以 jobName 和 jobGroup 作为唯一key
        JobKey jobKey = JobKey.jobKey(jobInfo.getJobName(), jobInfo.getJobGroup());

        try{
            //获取JobDetail
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if(jobDetail != null){
                //已存在
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            throw new BizException("检查任务是否存在时异常", e);
        }
    }

    /**
     * 根据JobInfo生成对应的触发器Trigger
     * @param jobInfo
     * @return
     */
    private Trigger genTrigger(JobInfoDto jobInfo){
        ScheduleBuilder scheduleBuilder = null;
        if(jobInfo.getJobType().equals(JobInfoDto.INTERVAL_JOB)){
            //用此处使用DailyTimeIntervalTrigger而不用SimpleTrigger是因为其SimpleTrigger的misfire机制不合理，如果应用宕机或重启，可能导致触发紊乱)
            DailyTimeIntervalScheduleBuilder dailyScheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                    .withMisfireHandlingInstructionDoNothing();//设置直接忽略错过的任务，因为错过的任务可以直接手动在管理后台执行
            if(jobInfo.getIntervals() != null){
                //设置间隔时间
                if(jobInfo.getIntervalUnit().equals(TimeUnitEnum.SECOND.getValue())){
                    dailyScheduleBuilder.withIntervalInSeconds(jobInfo.getIntervals());
                }else if(jobInfo.getIntervalUnit().equals(TimeUnitEnum.MINUTE.getValue())){
                    dailyScheduleBuilder.withIntervalInMinutes(jobInfo.getIntervals());
                }else if(jobInfo.getIntervalUnit().equals(TimeUnitEnum.HOUR.getValue())){
                    dailyScheduleBuilder.withIntervalInHours(jobInfo.getIntervals());
                }else{
                    throw new BizException(BizException.BIZ_INVALID, "UnSupported interval TimeUnit: " + jobInfo.getIntervalUnit());
                }
            }
            //设置重复次数
            if(jobInfo.getRepeatTimes() != null){
                dailyScheduleBuilder.withRepeatCount(jobInfo.getRepeatTimes());
            }
            scheduleBuilder = dailyScheduleBuilder;
        }else if(jobInfo.getJobType().equals(JobInfoDto.CRON_JOB)){ //按cronExpression表达式构建CronTrigger来触发任务
            //设置任务调度表达式
            scheduleBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();//设置直接忽略错过的任务，因为错过的任务可以直接手动在管理后台执行
        }else{
            throw new RuntimeException("未知的任务类型：" + jobInfo.getJobType());
        }

        //生成一个TriggerBuilder
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
                //用以生成Trigger的key
                .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
                .withSchedule(scheduleBuilder)
                .withDescription(jobInfo.getJobDescription());
        //设置开始、结束时间
        if(jobInfo.getStartTime() != null){
            triggerBuilder.startAt(jobInfo.getStartTime());
        }
        if(jobInfo.getEndTime() != null){
            triggerBuilder.endAt(jobInfo.getEndTime());
        }
        triggerBuilder.usingJobData(new JobDataMap(jobInfo.toMap()));//设置触发器的数据，用以同步到job_info表
        Trigger trigger = triggerBuilder.build();
        return trigger;
    }

    /**
     * 任务参数校验
     * @param jobInfo
     */
    private void checkJobParamAndSetDefaultValue(JobInfoDto jobInfo) {
        if (jobInfo == null) {
            throw new BizException(BizException.PARAM_INVALID, "scheduleJob不能为空");
        } else if (jobInfo.getJobType() == null) {
            throw new BizException(BizException.PARAM_INVALID, "任务类型(jobType)不能为空");
        } else if (StringUtil.isEmpty(jobInfo.getJobGroup())) {
            throw new BizException(BizException.PARAM_INVALID, "任务的组名(jobGroup)不能为空");
        } else if(! StringUtil.isNormalLetter(jobInfo.getJobGroup())){
            throw new BizException(BizException.PARAM_INVALID, "任务的组名(jobGroup)不允许出现中文和特殊字符");
        } else if (StringUtil.isEmpty(jobInfo.getJobName())) {
            throw new BizException(BizException.PARAM_INVALID, "任务名(jobName)不能为空");
        } else if(! StringUtil.isNormalLetter(jobInfo.getJobName())){
            throw new BizException(BizException.PARAM_INVALID, "任务名(jobName)不允许出现中文和特殊字符");
        } else if (StringUtil.isEmpty(jobInfo.getDestination())) {
            throw new BizException(BizException.PARAM_INVALID, "任务通知地址(destination)不能为空");
        } else if (jobInfo.getStartTime() == null) {
            throw new BizException(BizException.PARAM_INVALID, "开始时间(startTime)不能为空");
        }

        if (jobInfo.getJobType().equals(JobInfoDto.INTERVAL_JOB)) {
            if (jobInfo.getIntervals() == null) {
                throw new BizException(BizException.PARAM_INVALID, "任务间隔(interval)不能为空");
            } else if (jobInfo.getIntervalUnit() == null) {
                throw new BizException(BizException.PARAM_INVALID, "任务间隔单位(intervalUnit)不能为空");
            }
        } else if (jobInfo.getJobType().equals(JobInfoDto.CRON_JOB)) {
            if (StringUtil.isEmpty(jobInfo.getCronExpression())) {
                throw new BizException(BizException.PARAM_INVALID, "cron表达式(cronExpression)不能为空");
            }
        } else {
            throw new BizException(BizException.PARAM_INVALID, "未支持的任务类型jobType: " + jobInfo.getJobType());
        }

        if(!jobInfo.isActiveMQDestination() && !jobInfo.isRocketMQDestination() && !jobInfo.isRabbitMQDestination() && !jobInfo.isHttpDestination()){
            throw new BizException(BizException.PARAM_INVALID, "未支持的任务通知地址(destination): " + jobInfo.getDestination());
        }

        if(jobInfo.getRepeatTimes() == null){
            jobInfo.setRepeatTimes(JobInfoDto.REPEAT_FOREVER_INTERVAL);
        }
        if (jobInfo.getJobType().equals(JobInfoDto.INTERVAL_JOB)) {
            jobInfo.setCronExpression(null);
        } else if (jobInfo.getJobType().equals(JobInfoDto.CRON_JOB)) {
            jobInfo.setIntervals(null);
            jobInfo.setIntervalUnit(null);
        }
    }
}
