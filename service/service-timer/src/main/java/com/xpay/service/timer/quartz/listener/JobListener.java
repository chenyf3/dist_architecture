package com.xpay.service.timer.quartz.listener;

import com.xpay.service.timer.quartz.processor.EventProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局任务监听器
 * @author chenyf on 2020/2/1.
 */
public class JobListener implements org.quartz.JobListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private EventProcessor eventProcessor;

    public JobListener(EventProcessor eventProcessor){
        this.eventProcessor = eventProcessor;
    }

    /**
     * 返回当前监听器的名字，这个方法必须被写他的返回值；
     * 因为listener需要通过其getName()方法广播它的名称
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 任务被触发前触发
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        logger.debug("任务将要执行 jobGroup={} jobName={}", context.getJobDetail().getKey().getGroup(), context.getJobDetail().getKey().getName());
    }

    /**
     * 这个方法正常情况下不执行,但是如果当TriggerListener中的vetoJobExecution方法返回true时,那么执行这个方法.
     * 需要注意的是 如果这个方法被执行 那么jobToBeExecuted、jobWasExecuted这两个方法不会执行,因为任务被终止了嘛
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.debug("任务被否决 jobGroup={} jobName={}", context.getJobDetail().getKey().getGroup(), context.getJobDetail().getKey().getName());
    }

    /**
     * 任务调度完成后触发
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException){
        logger.info("任务执行完成 jobGroup={} jobName={}", context.getJobDetail().getKey().getGroup(), context.getJobDetail().getKey().getName());
    }
}

