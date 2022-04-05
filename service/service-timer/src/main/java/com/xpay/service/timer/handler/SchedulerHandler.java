package com.xpay.service.timer.handler;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.timer.dto.InstanceDto;
import com.xpay.facade.timer.dto.JobInfoDto;
import com.xpay.facade.timer.dto.OpLogDto;
import com.xpay.facade.timer.enums.OpTypeEnum;
import com.xpay.facade.timer.service.TimerAdminFacade;
import com.xpay.service.timer.biz.ExtInstanceBiz;
import com.xpay.service.timer.biz.ExtJobInfoBiz;
import com.xpay.service.timer.biz.ExtOpLogBiz;
import com.xpay.service.timer.entity.Instance;
import com.xpay.service.timer.entity.JobInfo;
import com.xpay.service.timer.quartz.base.JobManager;
import com.xpay.service.timer.quartz.base.SchedulerManager;
import com.xpay.starter.generic.invoker.DubboServiceInvoker;
import com.xpay.starter.generic.invoker.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 可操作Quartz实例和拓展实例的类
 * @author chenyf
 */
@Component
public class SchedulerHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    SchedulerManager schedulerManager;
    @Autowired
    JobManager jobManager;

    @Autowired
    ExtInstanceBiz extInstanceBiz;
    @Autowired
    ExtJobInfoBiz extJobInfoBiz;
    @Autowired
    ExtOpLogBiz extOpLogBiz;
    @Autowired
    DubboServiceInvoker dubboServiceInvoker;

    public InstanceDto getInstanceByInstanceId(String instanceId){
        if(StringUtil.isEmpty(instanceId)){
            return null;
        }
        Instance instance = extInstanceBiz.getByInstanceId(instanceId);
        return BeanUtil.newAndCopy(instance, InstanceDto.class);
    }

    public boolean pauseInstance(String instanceId, String remark) {
        boolean isSuccess = schedulerManager.pauseScheduler(instanceId);//如果要暂停的实例等于当前实例，则会返回true
        if(isSuccess){
            extInstanceBiz.pauseExtInstance();//这个处理比较特殊，不使用Quartz事件同步的方式，而直接调用方法处理，主要是避免混淆应用关闭时的standBy事件和人工standBy事件
            extOpLogBiz.addAsync(instanceId, remark, OpTypeEnum.PAUSE, "暂停实例");
        }else{
            //要暂停的实例不等于当前机器下的实例，则根据instanceId从实例记录中取得RPC地址，发起RPC远程操作调用
            Object obj = doInstanceRpcOperate(instanceId, remark, TimerAdminFacade.class.getName(), "pauseInstance");
            isSuccess = (boolean) obj;
        }
        return isSuccess;
    }

    public boolean resumeInstance(String instanceId, String remark){
        boolean isSuccess = schedulerManager.resumeScheduler(instanceId);
        if(isSuccess){
            extOpLogBiz.addAsync(instanceId, remark, OpTypeEnum.RESUME, "恢复实例");
        }else{
            Object obj = doInstanceRpcOperate(instanceId, remark, TimerAdminFacade.class.getName(), "resumeInstance");
            isSuccess = (boolean) obj;
        }
        return isSuccess;
    }

    public void scheduleJob(JobInfoDto jobInfo, String remark){
        jobManager.scheduleJob(jobInfo);
        extOpLogBiz.addAsync(jobInfo.getUniqueKey(), remark, OpTypeEnum.ADD, "添加任务");
    }

    public void rescheduleJob(JobInfoDto jobInfo, String remark){
        jobManager.rescheduleJob(jobInfo);
        extOpLogBiz.addAsync(jobInfo.getUniqueKey(), remark, OpTypeEnum.EDIT, "修改任务");
    }

    public void deleteJob(String jobGroup, String jobName, String remark){
        jobManager.deleteJob(jobGroup, jobName);
        extOpLogBiz.addAsync(JobInfoDto.getUniqueKey(jobGroup, jobName), remark, OpTypeEnum.DEL, "删除任务");
    }

    public void pauseJob(String jobGroup, String jobName, String remark){
        jobManager.pauseJob(jobGroup, jobName);
        extOpLogBiz.addAsync(JobInfoDto.getUniqueKey(jobGroup, jobName), remark, OpTypeEnum.PAUSE, "暂停任务");
    }

    public void resumeJob(String jobGroup, String jobName, String remark){
        jobManager.resumeJob(jobGroup, jobName);
        extOpLogBiz.addAsync(JobInfoDto.getUniqueKey(jobGroup, jobName), remark, OpTypeEnum.RESUME, "恢复任务");
    }

    public void triggerJob(String jobGroup, String jobName, String remark){
        if(schedulerManager.isStandByMode()){
            sendJobNotify(jobGroup, jobName, remark);
        }else{
            jobManager.triggerJob(jobGroup, jobName);
            extOpLogBiz.addAsync(JobInfoDto.getUniqueKey(jobGroup, jobName), remark, OpTypeEnum.EXE, "触发任务");
        }
    }

    public boolean sendJobNotify(String jobGroup, String jobName, String remark) {
        Map<String, Object> jobDataMap = jobManager.getJobDataMap(jobGroup, jobName);
        boolean isOk = extJobInfoBiz.sendJobNotify(jobDataMap);
        if(isOk){
            extOpLogBiz.addAsync(JobInfoDto.getUniqueKey(jobGroup, jobName), remark, OpTypeEnum.NOTIFY, "发送通知");
        }
        return isOk;
    }

    /**
     * 分页查询实例列表
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<InstanceDto>> listInstancePage(Map<String, Object> paramMap, PageQuery pageQuery){
        return extInstanceBiz.listInstancePage(paramMap, pageQuery);
    }

    public JobInfoDto getJobInfoByName(String jobGroup, String jobName){
        JobInfo jobInfo = extJobInfoBiz.getJobInfoByName(jobGroup, jobName);
        return BeanUtil.newAndCopy(jobInfo, JobInfoDto.class);
    }

    public PageResult<List<JobInfoDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) throws BizException {
        return extJobInfoBiz.listPage(paramMap, pageQuery);
    }

    /**
     * 分页查询操作日志
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public PageResult<List<OpLogDto>> listOpLogPage(Map<String, Object> paramMap, PageQuery pageQuery){
        return extOpLogBiz.listOpLogPage(paramMap, pageQuery);
    }

    /**
     * 操作远程实例，因为当前应用已经使用了Dubbo，所以直接使用dubbo来进行远程调用，也可以采用Quartz原生提供的RMI机制来实现
     * @param instanceId
     * @param remark
     * @param interfaceName
     * @param methodName
     * @return
     */
    private Object doInstanceRpcOperate(String instanceId, String remark, String interfaceName, String methodName){
        Instance instance = extInstanceBiz.getByInstanceId(instanceId);
        if(instance == null){
            throw new BizException("拓展实例记录不存在，instanceId: " + instanceId);
        }else if(StringUtil.isEmpty(instance.getUrl())){
            throw new BizException("拓展实例记录中url为空，不能发起RPC调用，instanceId: " + instanceId);
        }else{
            Instance extInstance = extInstanceBiz.getExtInstance();
            if(removeParamFromUrl(extInstance.getUrl()).equalsIgnoreCase(removeParamFromUrl(instance.getUrl()))){
                throw new BizException("远程调用地址和当前实例地址相同，无法发起远程调用");//避免进入死循环的调用
            }
        }

        logger.info("发起RPC远程调用 instanceId={} rpcAddress={} interfaceName={} methodName={}  ", instanceId, instance.getUrl(), interfaceName, methodName);
        Parameters parameters = Parameters.newInstance()
                .addParameter(String.class.getName(), instanceId)
                .addParameter(String.class.getName(), remark);

        return dubboServiceInvoker.invoke(instance.getUrl(), interfaceName, methodName, parameters);
    }

    private String removeParamFromUrl(String url){
        String[] urlArr = url.split("\\?");
        return urlArr[0];
    }
}
