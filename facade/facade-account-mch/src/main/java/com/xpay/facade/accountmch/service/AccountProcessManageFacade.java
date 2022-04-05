package com.xpay.facade.accountmch.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.facade.accountmch.dto.AccountProcessPendingDto;
import com.xpay.facade.accountmch.dto.AccountProcessResultDto;
import com.xpay.common.statics.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * @description: 平台商户账务处理管理接口
 * @author: chenyf
 * @date: 2019-02-12
 */
public interface AccountProcessManageFacade {

    /**
     * 分页查询待账务处理表记录
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<AccountProcessPendingDto>> listAccountProcessPendingPage(Map<String, Object> paramMap, PageQuery pageQuery);

    /**
     * 分页查询待账务处理历史表记录
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<AccountProcessPendingDto>> listAccountProcessPendingHistoryPage(Map<String, Object> paramMap, PageQuery pageQuery);

    /**
     * 根据id查询待账务处理表记录
     * @param id
     * @return
     */
    public AccountProcessPendingDto getProcessPendingById(Long id);

    /**
     * 把待账务处理记录从处理中审核为待处理
     * @param id
     * @return
     */
    public boolean auditProcessPendingFromProcessingToPending(Long id);

    /**
     * 分页查询账务处理结果表记录
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<AccountProcessResultDto>> listProcessResultPage(Map<String, Object> paramMap, PageQuery pageQuery);

    /**
     * 分页查询账务处理结果历史表记录
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<AccountProcessResultDto>> listProcessResultHistoryPage(Map<String, Object> paramMap, PageQuery pageQuery);

    /**
     * 根据id查询账务处理结果表记录
     * @param id
     * @return
     */
    public AccountProcessResultDto getAccountProcessResultById(Long id);

    /**
     * 根据账务处理明细来审核账务处理结果
     * @param id
     * @param reprocessWhenFail   如果账务处理失败，是否重新执行账务处理(仅对异步账务处理有效)
     * @return
     */
    public boolean checkProcessDetailToAuditProcessResult(Long id, boolean reprocessWhenFail);

    /**
     * 发送处理结果的回调通知并更新记录状态为“已发送”
     * @param id
     * @return
     */
    public boolean sendProcessResultCallbackMsg(Long id);

    /**
     * 添加账务处理结果，当出现一些比较极端的情况(如：数据库连接池不足等)，导致账务处理完成，但是没有生成账务处理结果时，可使用
     * @param processResult
     * @return
     */
    public boolean addAccountProcessResult(AccountProcessResultDto processResult);
}
