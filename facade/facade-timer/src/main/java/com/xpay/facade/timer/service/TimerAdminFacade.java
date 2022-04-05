package com.xpay.facade.timer.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.timer.dto.OpLogDto;
import com.xpay.facade.timer.dto.InstanceDto;

import java.util.List;
import java.util.Map;

/**
 * Quartz实例管理接口
 */
public interface TimerAdminFacade {
    /**
     * 根据实例id获取实例记录
     * @param instanceId
     * @return
     */
    public InstanceDto getInstanceByInstanceId(String instanceId);

    /**
     * 挂起实例，instanceId所指定的实例不执行任务任务
     * @param instanceId        实例Id，应用的实例Id取决于 org.quartz.scheduler.instanceId 配置项，默认是AUTO，将会以 hostname + 毫秒级时间戳 作为实例Id
     * @param remark            备注
     * @return
     * @throws BizException
     */
    public boolean pauseInstance(String instanceId, String remark) throws BizException;

    /**
     * 恢复实例(即结束挂起状态)，会自动清除持久化的pause状态
     * @param instanceId        实例Id，应用的实例Id取决于 org.quartz.scheduler.instanceId 配置项，默认是AUTO，将会以 hostname + 毫秒级时间戳 作为实例Id
     * @param remark            备注
     * @return
     * @throws BizException
     */
    public boolean resumeInstance(String instanceId, String remark) throws BizException;

    /**
     * 分页查询实例列表
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<InstanceDto>> listInstancePage(Map<String, Object> paramMap, PageQuery pageQuery) throws BizException;

    /**
     * 分页查询操作日志
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public PageResult<List<OpLogDto>> listOpLogPage(Map<String, Object> paramMap, PageQuery pageQuery) throws BizException;
}
