package com.xpay.service.accountmch.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.accountmch.dto.AccountProcessPendingDto;
import com.xpay.facade.accountmch.dto.AccountProcessResultDto;
import com.xpay.facade.accountmch.service.AccountProcessManageFacade;
import com.xpay.service.accountmch.biz.AccountProcessPendingBiz;
import com.xpay.service.accountmch.biz.AccountProcessResultBiz;
import com.xpay.service.accountmch.biz.AccountQueryBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class AccountProcessManageFacadeImpl implements AccountProcessManageFacade {
    @Autowired
    AccountQueryBiz accountQueryBiz;
    @Autowired
    AccountProcessPendingBiz accountProcessPendingBiz;
    @Autowired
    AccountProcessResultBiz accountProcessResultBiz;

    @Override
    public PageResult<List<AccountProcessPendingDto>> listAccountProcessPendingPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return accountProcessPendingBiz.listByPage(paramMap, pageQuery);
    }

    @Override
    public PageResult<List<AccountProcessPendingDto>> listAccountProcessPendingHistoryPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return accountProcessPendingBiz.listHistoryByPage(paramMap, pageQuery);
    }

    @Override
    public AccountProcessPendingDto getProcessPendingById(Long id) {
        return accountProcessPendingBiz.getAccountProcessPendingById(id);
    }

    @Override
    public boolean auditProcessPendingFromProcessingToPending(Long id) {
        return accountProcessPendingBiz.auditProcessPendingFromProcessingToPending(id);
    }

    @Override
    public PageResult<List<AccountProcessResultDto>> listProcessResultPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return accountProcessResultBiz.listByPage(paramMap, pageQuery);
    }

    @Override
    public PageResult<List<AccountProcessResultDto>> listProcessResultHistoryPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return accountProcessResultBiz.listHistoryByPage(paramMap, pageQuery);
    }

    @Override
    public AccountProcessResultDto getAccountProcessResultById(Long id) {
        return accountProcessResultBiz.getAccountProcessResultDtoById(id);
    }

    @Override
    public boolean checkProcessDetailToAuditProcessResult(Long id, boolean reprocessWhenFail) {
        return accountProcessResultBiz.checkProcessDetailToAuditProcessResult(id, reprocessWhenFail);
    }

    @Override
    public boolean sendProcessResultCallbackMsg(Long id) {
        return accountProcessResultBiz.sendProcessResultCallbackMsg(id);
    }

    @Override
    public boolean addAccountProcessResult(AccountProcessResultDto processResult) {
        return accountProcessResultBiz.addAccountProcessResult(processResult);
    }
}
