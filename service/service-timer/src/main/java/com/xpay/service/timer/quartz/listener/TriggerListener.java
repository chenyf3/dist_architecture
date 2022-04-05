package com.xpay.service.timer.quartz.listener;

import com.xpay.common.utils.JsonUtil;
import com.xpay.service.timer.quartz.processor.EventProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局触发器监听器
 * @author chenyf on 2020/2/1.
 */
public class TriggerListener implements org.quartz.TriggerListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private EventProcessor eventProcessor;

    public TriggerListener(EventProcessor eventProcessor){
        this.eventProcessor = eventProcessor;
    }

    /**
     * 监听器名称
     * @return
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 被调度时触发，和它相关的 org.quartz.jobDetail 即将执行。
     * 该方法优先vetoJobExecution()执行
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        logger.info("任务触发完成 jobGroup={} jobName={}", trigger.getJobKey().getGroup(), trigger.getJobKey().getName());
    }

    /**
     * 可根据实体业务情况来决定否决job，返回true时表示不执行当前任务，返回false时和它相关的 org.quartz.jobDetail 将被执行。
     * @param trigger
     * @param context
     * @return
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        logger.debug("触发器判断是否忽略此次触发 jobGroup={} jobName={}", trigger.getKey().getGroup(), trigger.getKey().getName());
        return false;
    }

    /**
     * 处理misfire的
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        if(! isKeyEqual(trigger.getJobKey(), trigger.getKey())){
            return;
        }

        //只要是被认定为misfire的任务在有机会时(获取到线程资源或者重启应用)都会被重新触发，然后各个任务再根据各自配置的策略来做出相应的处理
        logger.info("misfire事件发生,更新下次执行时间 jobGroup={} jobName={}", trigger.getKey().getGroup(), trigger.getKey().getName());
        String jobGroup = trigger.getKey().getGroup();
        String jobName = trigger.getKey().getName();

        Map<String, Object> jobProperties = new HashMap<>(50);
        jobProperties.put("nextExecuteTime", trigger.getFireTimeAfter(new Date()));
        logger.trace("更新任务相关属性, jobGroup={} jobName={} jobProperties={}", jobGroup, jobName, JsonUtil.toJsonFriendly(jobProperties));
        eventProcessor.afterJobMisfired(jobGroup, jobName, jobProperties);
    }

    /**
     * 触发器执行完毕时触发
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction var3) {
        String jobGroup = trigger.getJobKey().getGroup();//要取JobKey才行
        String jobName = trigger.getJobKey().getName();

        try{
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger triggerOri = context.getScheduler().getTrigger(triggerKey);
            Trigger.TriggerState stage = context.getScheduler().getTriggerState(triggerKey);
            Map<String, Object> jobProperties = new HashMap<>(8);
            jobProperties.put("jobStatus", stage.name());
            jobProperties.put("lastExecuteTime", triggerOri.getPreviousFireTime());
            jobProperties.put("nextExecuteTime", triggerOri.getNextFireTime());
            eventProcessor.afterJobExecuted(jobGroup, jobName, jobProperties);
        }catch(Exception e){
            logger.error("触发器触发完成更新信息时出现异常 jobGroup={} jobName={}", jobGroup, jobName, e);
        }
    }

    /**
     * 在调用org.quartz.Scheduler.triggerJob(JobKey jobKey)方法来触发任务时，Quartz内部是为这个Job生成了一个立即执行的临时Trigger
     * 此时，这个临时Trigger的group和name跟JobKey的group和name是不一样的
     * @param jobKey
     * @param triggerKey
     * @return
     */
    private boolean isKeyEqual(JobKey jobKey, TriggerKey triggerKey){
        return jobKey.getGroup().equals(triggerKey.getGroup()) && jobKey.getName().equals(triggerKey.getName());
    }
}
