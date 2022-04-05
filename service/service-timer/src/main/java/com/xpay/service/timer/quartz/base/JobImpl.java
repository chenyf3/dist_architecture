package com.xpay.service.timer.quartz.base;

import com.xpay.service.timer.quartz.processor.EventProcessor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz的任务执行类：
 *   说明：
 *     1、此项目只是作为定时任务的触发器，而真正执行业务逻辑的方法或者类并不在本项目中，而是分散到各自的业务系统中，当定时任务触发，会通过发送MQ消息
 *      或者http请求等形式来通知各个业务进行相应的业务处理。
 *     2、对于不允许并发执行的任务，由各个业务系统自行处理，比如：用分布式锁机制，真正执行业务逻辑之前先去获取锁，如果获取不到，说明此任务正在执行中，
 *     就根据业务实际需求来选择直接忽略此消息还是把消息重新入列
 *     3、如果当前应用在多台服务器上都进行了部署，那就需要开启集群模式，并且每台服务器的时间要保持一致，避免多台机器同时触发了任务
 *     4、Quartz在触发任务的时候，默认是每一次执行都新建一个Job实例对象，所以如果本类有属性需要从JobDataMap中注入，这是线程安全的
 * @author chenyf
 */
public class JobImpl implements Job {
    private EventProcessor processor;

    public JobImpl(EventProcessor extendProcessor){
        this.processor = extendProcessor;
    }

    @Override
    public void execute(JobExecutionContext context) {
        processor.doJobExecute(context.getTrigger().getJobDataMap().getWrappedMap());
    }
}
