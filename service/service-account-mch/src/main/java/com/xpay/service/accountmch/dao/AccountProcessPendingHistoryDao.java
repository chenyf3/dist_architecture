/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.accountmch.entity.AccountProcessPending;
import com.xpay.service.accountmch.entity.AccountProcessPendingHistory;
import org.springframework.stereotype.Repository;

@Repository
public class AccountProcessPendingHistoryDao extends MyBatisDao<AccountProcessPending, Long> {
    @Override
    protected void setMapperNamespace(String mapperNamespace) {
        super.setMapperNamespace(AccountProcessPendingHistory.class.getName());
    }
}
