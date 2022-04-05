/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.dto.account.AccountDetailDto;
import com.xpay.common.utils.DateUtil;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import com.xpay.service.accountmch.entity.AccountProcessDetailHistory;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountProcessDetailHistoryDao extends MyBatisDao<AccountProcessDetail, Long> {
    @Override
    protected void setMapperNamespace(String mapperNamespace) {
        super.setMapperNamespace(AccountProcessDetailHistory.class.getName());
    }

    public List<AccountDetailDto> listDetailDtoByAccountNoAndRequestNo(String accountNo, String requestNo, Date dateBegin){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("accountNo", accountNo);
        paramMap.put("requestNo", requestNo);
        paramMap.put("createTimeBegin", DateUtil.formatDate(dateBegin));
        return listBy("listDetailDtoByAccountNoAndRequestNo", paramMap);
    }

    public boolean isAccountProcessDetailExist(String accountNo, String requestNo, Integer processType, String processNo, Date dateBegin){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("accountNo", accountNo);
        paramMap.put("requestNo", requestNo);
        paramMap.put("processType", processType);
        paramMap.put("processNo", processNo);
        paramMap.put("createTimeBegin", DateUtil.formatDate(dateBegin));
        return getOne(paramMap) != null;
    }
}
