package com.xpay.service.timer.quartz.listener;

import com.xpay.service.timer.quartz.processor.EventProcessor;
import org.quartz.*;

import java.util.Map;

/**
 * 全局实例监听器
 * @author chenyf
 */
public class SchedulerListener implements org.quartz.SchedulerListener {
    private EventProcessor eventProcessor;

    public SchedulerListener(EventProcessor eventProcessor){
        this.eventProcessor = eventProcessor;
    }

    /**
     * 任务添加或修改事件
     * @param trigger
     */
    @Override
    public void jobScheduled(Trigger trigger) {
        if(! isKeyEqual(trigger.getJobKey(), trigger.getKey())){
            return;
        }
        Map<String, Object> jobDataMap = trigger.getJobDataMap().getWrappedMap();
        jobDataMap.put("nextExecuteTime", trigger.getNextFireTime());
        eventProcessor.afterJobAddOrUpdate(jobDataMap);
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {

    }

    /**
     * 当trigger终结时
     * @param trigger
     */
    @Override
    public void triggerFinalized(Trigger trigger) {

    }

    /**
     * 触发器被暂停事件
     * @param triggerKey
     */
    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        eventProcessor.afterJobPause(triggerKey.getGroup(), triggerKey.getName());
    }

    @Override
    public void triggersPaused(String triggerGroup) {

    }

    /**
     * 触发器被恢复事件
     * @param triggerKey
     */
    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        eventProcessor.afterJobResume(triggerKey.getGroup(), triggerKey.getName());
    }

    @Override
    public void triggersResumed(String triggerGroup) {

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {

    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        eventProcessor.afterJobDelete(jobKey.getGroup(), jobKey.getName());
    }

    @Override
    public void jobPaused(JobKey jobKey) {

    }

    @Override
    public void jobsPaused(String jobGroup) {

    }

    @Override
    public void jobResumed(JobKey jobKey) {

    }

    @Override
    public void jobsResumed(String jobGroup) {

    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {

    }

    /**
     * 实例挂起事件
     */
    @Override
    public void schedulerInStandbyMode() {
        eventProcessor.afterSchedulePause();
    }

    /**
     * 实例启动中事件
     */
    @Override
    public void schedulerStarting() {
        eventProcessor.afterScheduleStarting();
    }

    /**
     * 实例启动完成事件
     */
    @Override
    public void schedulerStarted() {
        eventProcessor.afterScheduleRunning();
    }

    /**
     * 实例正在关闭事件
     */
    @Override
    public void schedulerShuttingdown() {

    }

    /**
     * 实例已关闭事件
     */
    @Override
    public void schedulerShutdown() {
        eventProcessor.afterScheduleShutdown();
    }

    @Override
    public void schedulingDataCleared() {
        
    }

    /**
     * 判断 JobKey 和 TriggerKey 是否相等，因为有些Job会有临时的Trigger
     * @param jobKey
     * @param triggerKey
     * @return
     */
    private boolean isKeyEqual(JobKey jobKey, TriggerKey triggerKey){
        return jobKey.getGroup().equals(triggerKey.getGroup()) && jobKey.getName().equals(triggerKey.getName());
    }
}
