package com.xpay.facade.timer.dto;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf on 2020/4/26.
 */
public class JobInfoDto implements Serializable {
    private static final long serialVersionUID = 43493082485945483L;

    /**
     * 间隔任务，比较适合按一定间隔重复执行的任务
     */
    public static final int INTERVAL_JOB = 1;

    /**
     * cron任务，适合除短间隔以外的任务（cron表达式在秒级别重复执行的时候会有丢失精度的问题）
     */
    public static final int CRON_JOB = 2;

    /**
     * 永远重复任务的重复次数值
     */
    public static final int REPEAT_FOREVER_INTERVAL = -1;

    /**
     * ID主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime = new Date();

    /**
     * 版本号
     */
    private Long version = 0L;

    /**
     * 任务分组
     */
    private String jobGroup;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务类型 1=simple任务 2=cron任务
     */
    private Integer jobType;

    /**
     * 通知地址，支持 http/https/amq/rmq 作为前缀，如：amq://timer.notify.simpleJob、http://www.example.org/notify
     */
    private String destination;

    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务结束时间
     */
    private Date endTime;

    /**
     * 任务间隔
     */
    private Integer intervals;

    /**
     * 任务间隔的单位
     */
    private Integer intervalUnit;

    /**
     * 重复次数
     */
    private Integer repeatTimes;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 任务状态(参考org.quartz.Trigger.TriggerState)
     */
    private String jobStatus;

    /**
     * json格式的参数
     */
    private String paramJson;

    /**
     * 任务描述
     */
    private String jobDescription;

    /**
     * 上次执行时间
     */
    private Date lastExecuteTime;

    /**
     * 下次执行时间
     */
    private Date nextExecuteTime;

    /**
     * 已执行次数
     */
    private Long executedTimes;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getIntervals() {
        return intervals;
    }

    public void setIntervals(Integer intervals) {
        this.intervals = intervals;
    }

    public Integer getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(Integer intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Integer getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(Integer repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getParamJson() {
        return paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Date getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(Date lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
    }

    public Date getNextExecuteTime() {
        return nextExecuteTime;
    }

    public void setNextExecuteTime(Date nextExecuteTime) {
        this.nextExecuteTime = nextExecuteTime;
    }

    public Long getExecutedTimes() {
        return executedTimes;
    }

    public void setExecutedTimes(Long executedTimes) {
        this.executedTimes = executedTimes;
    }

    public JobInfoDto(){}

    public JobInfoDto(String jobGroup, String jobName){
        this.jobGroup = jobGroup;
        this.jobName = jobName;
    }

    /**
     * 返回一个间隔任务业务对象
     * @return
     */
    public static JobInfoDto newIntervalTask(String jobGroup, String jobName, String destination){
        JobInfoDto job = new JobInfoDto();
        job.setJobGroup(jobGroup);
        job.setJobName(jobName);
        job.setDestination(destination);
        job.setJobType(JobInfoDto.INTERVAL_JOB);
        job.setStartTime(new Date());
        return job;
    }

    /**
     * 返回一个Cron任务业务对象
     * @return
     */
    public static JobInfoDto newCronTask(String jobGroup, String jobName, String destination){
        JobInfoDto job = new JobInfoDto();
        job.setJobGroup(jobGroup);
        job.setJobName(jobName);
        job.setDestination(destination);
        job.setJobType(JobInfoDto.CRON_JOB);
        job.setStartTime(new Date());
        return job;
    }

    /**
     * 永远重复的任务：即从某个时间点开始一直重复下去的任务
     * @param startTime
     * @param intervals
     * @param intervalUnit
     * @return
     */
    public void setToRepeatForeverTask(Date startTime, Integer intervals, Integer intervalUnit){
        this.setStartTime(startTime);
        this.setIntervals(intervals);
        this.setIntervalUnit(intervalUnit);
        this.setRepeatTimes(JobInfoDto.REPEAT_FOREVER_INTERVAL);
        this.setEndTime(null);
    }

    /**
     * 在某个时间段按某个频率重复的任务：即从某个时间开始按某个间隔重复，到达结束时间后结束任务
     * @param startTime
     * @param intervals
     * @param intervalUnit
     * @param endTime
     * @return
     */
    public void setToRepeatPeriodTask(Date startTime, Integer intervals, Integer intervalUnit, Date endTime){
        this.setStartTime(startTime);
        this.setIntervals(intervals);
        this.setIntervalUnit(intervalUnit);
        this.setEndTime(endTime);
    }

    /**
     * 在某个时间段按某个频率重复的任务：即从某个时间开始按某个间隔重复一定次数，如果次数已执行完，但还没到结束时间，则任务结束，如果次数还没执行完，但已到结束时间，任务也结束
     * @param startTime
     * @param intervals
     * @param intervalUnit
     * @param repeatTimes
     * @param endTime
     * @return
     */
    public void setToRepeatPeriodTask(Date startTime, Integer intervals, Integer intervalUnit, Integer repeatTimes, Date endTime){
        this.setStartTime(startTime);
        this.setIntervals(intervals);
        this.setIntervalUnit(intervalUnit);
        this.setRepeatTimes(repeatTimes);
        this.setEndTime(endTime);
    }

    /**
     * 重复一定次数的任务：即从某个时间开始按某个间隔重复一定次数后结束任务
     * @param startTime
     * @param intervals
     * @param intervalUnit
     * @param repeatTimes
     * @return
     */
    public void setToRepeatLimitTask(Date startTime, Integer intervals, Integer intervalUnit, Integer repeatTimes){
        this.setStartTime(startTime);
        this.setIntervals(intervals);
        this.setIntervalUnit(intervalUnit);
        this.setRepeatTimes(repeatTimes);
        this.setEndTime(null);
    }

    /**
     * cron表达式任务：即从某个时间开始按cron表达式触发，到达结束时间后结束任务
     * @param startTime
     * @param cronExpression
     * @param endTime
     * @return
     */
    public void setToCronTask(Date startTime, String cronExpression, Date endTime){
        this.setStartTime(startTime);
        this.setCronExpression(cronExpression);
        this.setEndTime(endTime);
    }

    /**
     * cron表达式任务：即从某个时间开始按cron表达式触发执行的任务
     * @param startTime
     * @param cronExpression
     * @return
     */
    public void setToCronTask(Date startTime, String cronExpression){
        this.setStartTime(startTime);
        this.setCronExpression(cronExpression);
        this.setEndTime(null);
    }

    public boolean isActiveMQDestination(){
        return this.destination != null && this.destination.startsWith("amq://");
    }

    public boolean isRocketMQDestination(){
        return this.destination != null && this.destination.startsWith("rmq://");
    }

    public boolean isRabbitMQDestination(){
        return this.destination != null && this.destination.startsWith("raq://");
    }
    
    public boolean isHttpDestination(){
        return this.destination != null && (this.destination.startsWith("http://") || this.destination.startsWith("https://"));
    }

    public String getUniqueKey(){
        return jobGroup + ":" + jobName;
    }

    public static String getUniqueKey(String jobGroup, String jobName){
        return jobGroup + ":" + jobName;
    }

    public Map<String, String> toMap(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, String> map = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);

            Object obj = null;
            try{
                obj = field.get(this);
            }catch(Exception e){
            }

            String value = null;
            if(obj == null){
                value = null;
            }else if(obj instanceof String){
                value = (String) obj;
            }else if(obj instanceof Date){
                value = sdf.format((Date)obj);
            }else{
                value = obj.toString();
            }

            map.put(field.getName(), value);
        }
        return map;
    }
}
