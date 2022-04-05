package com.xpay.service.timer.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.IPUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.timer.enums.OpTypeEnum;
import com.xpay.facade.timer.enums.TimerStatus;
import com.xpay.service.timer.config.ExtProperties;
import com.xpay.service.timer.dao.InstanceDao;
import com.xpay.service.timer.entity.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xpay.facade.timer.dto.InstanceDto;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实例操作逻辑层，这个表等同于Quartz中的Scheduler，任何对Scheduler的操作(挂起、恢复、关闭等)都会同步到ext_instance表，
 * 同时该表还会记录每个实例自己的RPC地址，方便对实例进行管理
 * 注：极端情况下(如：数据库连接池不足、网络不好等)可能会出现Quartz中修改成功但是同步到ext_instance表时失败的情况，此时可人工介入处理
 * @author chenyf
 */
@Component
public class ExtInstanceBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private ExtProperties properties = null;

    @Autowired
    ExtOpLogBiz extOpLogBiz;
    @Autowired
    InstanceDao instanceDao;

    /**
     * 初始化拓展实例
     * @param properties
     * @return
     */
    public synchronized void initExtInstance(ExtProperties properties){
        //1.参数校验
        if(this.properties != null){
            throw new BizException("当前实例已初始化！");
        }else if(properties == null){
            throw new BizException("properties不能为空");
        }else if(StringUtil.isEmpty(properties.getInstanceId())){
            throw new BizException("instanceId不能为空");
        }else if(StringUtil.isEmpty(properties.getRpcAddress())){
            throw new BizException("rpcAddress不能为空");
        }else if(properties.getCheckInInterval() == null){
            throw new BizException("checkInInterval不能为空");
        }else{
            this.properties = properties;
        }

        //2.参数准备
        final String instanceId = this.properties.getInstanceId();
        String rpcAddress = this.properties.getRpcAddress();
        Integer checkInInterval = this.properties.getCheckInInterval();
        if(checkInInterval < 1000){
            checkInInterval = 1000;//最小都需要1秒
        }

        //3.初始化实例记录
        Instance instance = instanceDao.getByInstanceId(instanceId);
        if(instance == null){
            insertInstance(instanceId, TimerStatus.RUNNING.getValue(), PublicStatus.ACTIVE, rpcAddress);
        }else{
            //更新实例的相关信息
            instance.setHost(IPUtil.getLocalHost());
            instance.setIp(IPUtil.getLocalIp());
            instance.setStatus(PublicStatus.ACTIVE);
            instance.setUrl(rpcAddress);
            instanceDao.update(instance);
        }

        //4.定期更新检入时间
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try{
                Boolean isOk = instanceDao.updateCheckInTime(instanceId);
                if(! isOk){ //如果有人直接从数据库中删除了此实例的记录，会出现这种情况
                    logger.error("instanceId={} 更新实例检入时间失败，请检查实例记录是否存在！", instanceId);
                }
            }catch(Throwable e){
                logger.error("instanceId={} 更新实例检入时间时出现异常", instanceId, e);
            }
        }, 1L, checkInInterval, TimeUnit.MILLISECONDS);

        //5.删除过期实例
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try{
                deleteDeathInstance(60);
            }catch(Throwable e){
                logger.error("删除过期实例时出现异常", e);
            }
        }, 1, 30, TimeUnit.MINUTES);
    }

    /**
     * 暂停实例(挂起实例)
     * @return
     */
    public boolean pauseExtInstance(){
        String instanceId = properties.getInstanceId();
        Instance instance = instanceDao.getByInstanceId(instanceId);
        if(instance == null){
            insertInstance(instanceId, TimerStatus.STAND_BY.getValue(), PublicStatus.ACTIVE, properties.getRpcAddress());
        }else if(instance.getScheduleStatus() == TimerStatus.STAND_BY.getValue()){
            logger.info("instanceId={} 当前实例已处于挂起中", instanceId);
            return true;
        }
        return instanceDao.updateScheduleStatus(instanceId, TimerStatus.STAND_BY.getValue());
    }

    /**
     * 运行实例(或恢复挂起的实例)
     * @return
     */
    public boolean runExtInstance(){
        String instanceId = properties.getInstanceId();
        Instance instance = instanceDao.getByInstanceId(instanceId);
        if(instance == null){
            insertInstance(instanceId, TimerStatus.RUNNING.getValue(), PublicStatus.ACTIVE, properties.getRpcAddress());
        }else if(instance.getScheduleStatus() == TimerStatus.RUNNING.getValue()){
            logger.info("instanceId={} 当前实例已处于运行中", instanceId);
            return true;
        }
        return instanceDao.updateScheduleStatus(instanceId, TimerStatus.RUNNING.getValue());
    }

    /**
     * 关闭拓展实例
     * @return
     */
    public boolean shutdownExtInstance(){
        String instanceId = properties.getInstanceId();
        Instance instance = instanceDao.getByInstanceId(instanceId);
        if(instance == null){
            return true;
        }else if(instance.getStatus() == PublicStatus.INACTIVE){
            logger.info("instanceId={} 当前实例已处于关闭中", instanceId);
            return true;
        }else{
            instance.setStatus(PublicStatus.INACTIVE);
            instanceDao.update(instance);
            return true;
        }
    }

    public boolean isExtInstanceStandBy(){
        if(properties.getInstanceId() == null){
            return false;
        }
        Instance instance = getByInstanceId(properties.getInstanceId());
        return instance != null && instance.getScheduleStatus() == TimerStatus.STAND_BY.getValue();
    }

    public Instance getExtInstance(){
        String instanceId = properties.getInstanceId();
        return getByInstanceId(instanceId);
    }

    public boolean deleteInstance(String instanceId, String operator){
        Instance instance = instanceDao.getByInstanceId(instanceId);
        if(instance == null){
            return true;
        }else if(isHealth(instance.getUpdateTime().getTime(), maxInactiveAllowTime())){
            throw new BizException("该实例状态健康，不能删除！");
        }

        instanceDao.deleteById(instance.getId());
        extOpLogBiz.addAsync(instanceId, operator, OpTypeEnum.DEL, "删除实例");
        return true;
    }

    /**
     * 删除已死亡的实例
     * @param overMinutes  超过这个参数代表的分钟数没有检入，就代表该实例已死亡
     */
    public void deleteDeathInstance(int overMinutes){
        if(overMinutes < 60){
            overMinutes = 60;
        }

        List<Instance> instanceList = instanceDao.listOverTimeInstance(overMinutes);//超过 overMinutes 没检入的实例当做是已死亡的实例
        if(instanceList == null || instanceList.isEmpty()){
            return;
        }
        for(Instance instance : instanceList){
            try{
                instanceDao.deleteById(instance.getId());
                extOpLogBiz.addAsync(instance.getInstanceId(), "system", OpTypeEnum.DEL, "删除失活实例");
            }catch(Throwable e){
                logger.error("过期实例删除异常 instanceId={} Exception={}", instance.getInstanceId(), e.getMessage());
            }
        }
    }

    public Instance getByInstanceId(String instanceId){
        return instanceDao.getByInstanceId(instanceId);
    }

    public PageResult<List<InstanceDto>> listInstancePage(Map<String, Object> paramMap, PageQuery pageQuery) {
        if (StringUtil.isEmpty(pageQuery.getSortColumns())) {
            pageQuery.setSortColumns("ID desc");
        }

        PageResult<List<Instance>> pageResult = instanceDao.listPage(paramMap, pageQuery);
        long inactiveTime = maxInactiveAllowTime();
        List<InstanceDto> dtoList = BeanUtil.newAndCopy(pageResult.getData(), InstanceDto.class);
        for (InstanceDto instance : dtoList) {
            instance.setIsHealth(this.isHealth(instance.getUpdateTime().getTime(), inactiveTime));
        }
        return PageResult.newInstance(dtoList, pageResult);
    }

    private Instance insertInstance(String instanceId, Integer scheduleStatus, Integer status, String url){
        Instance instance = new Instance();
        instance.setCreateTime(new Date());
        instance.setUpdateTime(instance.getCreateTime());
        instance.setInstanceId(instanceId);
        instance.setHost(IPUtil.getLocalHost());
        instance.setIp(IPUtil.getLocalIp());
        instance.setStatus(status);
        instance.setScheduleStatus(scheduleStatus);
        instance.setRemark("");
        instance.setUrl(url == null ? "" :  url);
        try {
            instanceDao.insert(instance);
            logger.info("拓展实例创建完成 instanceId={} scheduleStatus={} status={}", instanceId, scheduleStatus, status);
        } catch (Exception ex) {
            throw new BizException(BizException.PARAM_INVALID, "instanceId = " + instanceId + " 当前实例创建失败", ex);
        }
        return instance;
    }

    private boolean isHealth(long lastCheckTime, long intervals){
        if(System.currentTimeMillis() - lastCheckTime <= intervals){
            return true;
        }
        return false;
    }

    private long maxInactiveAllowTime(){
        return properties.getCheckInInterval() * 5;
    }
}
