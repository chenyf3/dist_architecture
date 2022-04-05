package com.xpay.service.timer.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.timer.dto.InstanceDto;
import com.xpay.facade.timer.dto.OpLogDto;
import com.xpay.service.timer.handler.SchedulerHandler;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import com.xpay.facade.timer.service.TimerAdminFacade;

import java.util.List;
import java.util.Map;

@DubboService
public class TimerAdminFacadeImpl implements TimerAdminFacade {
    @Autowired
    SchedulerHandler schedulerHandler;

    public InstanceDto getInstanceByInstanceId(String instanceId){
        return schedulerHandler.getInstanceByInstanceId(instanceId);
    }

    @Override
    public boolean pauseInstance(String instanceId, String remark){
        return schedulerHandler.pauseInstance(instanceId, remark);
    }

    @Override
    public boolean resumeInstance(String instanceId, String remark){
        return schedulerHandler.resumeInstance(instanceId, remark);
    }

    /**
     * 分页查询实例列表
     * @param pageQuery
     * @param paramMap
     * @return
     */
    @Override
    public PageResult<List<InstanceDto>> listInstancePage(Map<String, Object> paramMap, PageQuery pageQuery){
        return schedulerHandler.listInstancePage(paramMap, pageQuery);
    }

    /**
     * 分页查询操作日志
     * @param paramMap
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult<List<OpLogDto>> listOpLogPage(Map<String, Object> paramMap, PageQuery pageQuery){
        return schedulerHandler.listOpLogPage(paramMap, pageQuery);
    }
}
