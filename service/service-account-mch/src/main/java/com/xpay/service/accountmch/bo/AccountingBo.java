package com.xpay.service.accountmch.bo;

import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessDetail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 账务处理过程中的业务对象
 */
public class AccountingBo {
    private Map<AccountMch, List<AccountProcessDetail>> accountMapDetail = new LinkedHashMap<>();//保持顺序

    public AccountingBo(){ }

    public Map<AccountMch, List<AccountProcessDetail>> getAccountMapDetail() {
        return accountMapDetail;
    }

    public void setAccountMapDetail(Map<AccountMch, List<AccountProcessDetail>> accountMapDetail) {
        this.accountMapDetail = accountMapDetail;
    }
}
