package com.xpay.service.accountmch.accounting;

import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.service.accountmch.accounting.processors.AccountingProcessor;

import java.util.Map;

/**
 * 账务处理策略选择器
 */
public class AccountingStrategy {
    private Map<Integer, AccountingProcessor> processorMap;

    public AccountingStrategy(Map<Integer, AccountingProcessor> processorMap){
        this.processorMap = processorMap;
    }

    public AccountingProcessor getProcessor(Integer processType){
        AccountingProcessor processor = processorMap.getOrDefault(processType, null);
        if(processor == null) {
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "无法选择处理器，未预期的processType: " + processType);
        }
        return processor;
    }
}
