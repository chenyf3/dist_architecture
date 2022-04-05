package com.xpay.service.accountmch.serviceImpl;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.accountmch.service.AccountProcessMchFacade;
import com.xpay.service.accountmch.biz.AccountProcessHandler;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class AccountProcessMchFacadeImpl implements AccountProcessMchFacade {
    @Autowired
    AccountProcessHandler accountProcessHandler;

    public boolean executeSync(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) throws BizException {
        return accountProcessHandler.process(requestDto, processDtoList);
    }

    public boolean executeAsync(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) throws BizException {
        return accountProcessHandler.bufferAsync(requestDto, processDtoList);
    }

    public boolean executeSync(Long processPendingId) throws BizException {
        return accountProcessHandler.process(processPendingId);
    }
}
