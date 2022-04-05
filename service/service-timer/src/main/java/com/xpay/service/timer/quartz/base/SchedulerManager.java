package com.xpay.service.timer.quartz.base;

import com.xpay.common.statics.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Quartz实例管理器
 * @author chenyf
 */
public class SchedulerManager {
    private Logger logger = LoggerFactory.getLogger(JobManager.class);
    private SchedulerFactoryBean schedulerFactoryBean;

    public SchedulerManager(SchedulerFactoryBean schedulerFactoryBean){
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    /**
     * 暂停实例，当前实例将不执行任务，如果是集群部署，当前实例上的任务会转移到集群中的其他实例上去
     * @param instanceId
     * @return
     */
    public boolean pauseScheduler(String instanceId){
        try{
            if(getSchedulerInstanceId().equals(instanceId)){
                if(! isStandByMode()){
                    schedulerFactoryBean.getScheduler().standby();
                }
                return true;
            }
        }catch(Throwable ex){
            logger.error("暂停实例失败", ex);
            throw BizException.unexpectException("暂停实例失败");
        }
        return false;
    }

    /**
     * 恢复实例
     * @param instanceId
     * @return
     */
    public boolean resumeScheduler(String instanceId){
        try{
            if(getSchedulerInstanceId().equals(instanceId)){
                if(isStandByMode()){
                    schedulerFactoryBean.getScheduler().start();
                }
                return true;
            }
        }catch(Throwable ex){
            logger.error("恢复实例失败", ex);
            throw new BizException(BizException.UNEXPECT_ERROR, "恢复实例失败");
        }
        return false;
    }

    /**
     * 判断当前实例是否处于挂起状态
     * @return
     */
    public boolean isStandByMode() {
        try{
            return schedulerFactoryBean.getScheduler().isInStandbyMode();
        }catch(Exception ex){
            logger.error("判断实例是否处于挂起状态时出现异常", ex);
            throw new BizException(BizException.UNEXPECT_ERROR, "判断实例是否处于挂起状态时出现异常");
        }
    }

    /**
     * 获取实例id
     * @return
     */
    public String getSchedulerInstanceId() {
        try{
            return schedulerFactoryBean.getScheduler().getSchedulerInstanceId();
        }catch(Exception ex){
            throw new BizException("获取instanceId异常", ex);
        }
    }

    public void startScheduler(){
        schedulerFactoryBean.start();
    }
}
