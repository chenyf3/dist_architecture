/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.accountmch.entity.AccountProcessResult;
import com.xpay.service.accountmch.entity.AccountProcessResultHistory;
import org.springframework.stereotype.Repository;

@Repository
public class AccountProcessResultHistoryDao extends MyBatisDao<AccountProcessResult, Long> {

    @Override
    protected void setMapperNamespace(String mapperNamespace) {
        super.setMapperNamespace(AccountProcessResultHistory.class.getName());
    }
}
