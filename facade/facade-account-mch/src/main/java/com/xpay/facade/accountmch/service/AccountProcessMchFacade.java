package com.xpay.facade.accountmch.service;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.exception.BizException;

import java.util.List;

public interface AccountProcessMchFacade {
    /**
     * 同步账务处理
     * @param requestDto
     * @param processDtoList
     * @return
     * @throws BizException
     */
    public boolean executeSync(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) throws BizException;

    /**
     * 异步账务处理
     * @param requestDto
     * @param processDtoList
     * @return
     * @throws BizException
     */
    public boolean executeAsync(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) throws BizException;

    /**
     * 同步账务处理（从待账务处理记录中拿取请求数据）
     * @param processPendingId
     * @return
     * @throws BizException
     */
    public boolean executeSync(Long processPendingId) throws BizException;

}
