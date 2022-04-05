package com.xpay.service.timer.quartz.processor;

import com.xpay.service.timer.biz.ExtInstanceBiz;
import com.xpay.service.timer.biz.ExtJobInfoBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Quartz事件处理器
 * @author chenyf
 */
@Component
public class EventProcessor {
    @Autowired
    private ExtJobInfoBiz extJobInfoBiz;
    @Autowired
    private ExtInstanceBiz extInstanceBiz;

    public void afterSchedulePause() {
        //此处不处理，因为在应用关闭前，Quartz也会先执行standBy()，这样会混淆人为standBy和应用关闭时的standBy行为，人为的standBy行为，
        //在实例下次重启的时候会依然保持standBy状态
    }

    public void afterScheduleStarting() {
    }

    public void afterScheduleRunning(){
        extInstanceBiz.runExtInstance();
    }

    public void afterScheduleShutdown(){
        extInstanceBiz.shutdownExtInstance();
    }

    public void afterJobAddOrUpdate(Map<String, Object> jobDataMap){
        extJobInfoBiz.addOrUpdateExtJob(jobDataMap);
    }

    public void afterJobMisfired(String jobGroup, String jobName, Map<String, Object> jobProperties){
        extJobInfoBiz.updatePropertiesAfterMisfire(jobGroup, jobName, jobProperties);
    }

    public void afterJobExecuted(String jobGroup, String jobName, Map<String, Object> jobProperties){
        extJobInfoBiz.updatePropertiesAfterExecuted(jobGroup, jobName, jobProperties);
    }

    public void afterJobPause(String jobGroup, String jobName){
        extJobInfoBiz.pauseExtJob(jobGroup, jobName);
    }

    public void afterJobResume(String jobGroup, String jobName){
        extJobInfoBiz.resumeExtJob(jobGroup, jobName);
    }

    public void afterJobDelete(String jobGroup, String jobName){
        extJobInfoBiz.deleteExtJob(jobGroup, jobName);
    }

    public void doJobExecute(Map<String, Object> jobDataMap){
        extJobInfoBiz.sendJobNotify(jobDataMap);
    }
}
