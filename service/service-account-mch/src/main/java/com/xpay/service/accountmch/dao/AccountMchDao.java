/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.accountmch.entity.AccountMch;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountMchDao extends MyBatisDao<AccountMch, Long> {

    public void updateAccountSnapTime(String accountNo, Integer nextSnapTime, Integer nextSnapTimeOld){
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("accountNo", accountNo);
        paramMap.put("nextSnapTime", nextSnapTime);
        paramMap.put("nextSnapTimeOld", nextSnapTimeOld);
        int count = update("updateAccountSnapTime", paramMap);
        if(count <= 0){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "修改快照时间失败！");
        }
    }

    public AccountMch getByAccountNo(String accountNo){
        if(StringUtil.isEmpty(accountNo)){
            return null;
        }
        return getOne("getByAccountNo", Collections.singletonMap("accountNo", accountNo));
    }

    public List<String> listAccountNoPage(Map<String, Object> paramMap, Integer pageCurrent, Integer pageSize, String sortColumn){
        int calPageCurrent = pageCurrent < 1 ? 0 : (pageCurrent - 1);
        Integer offset = calPageCurrent * pageSize;
        if(paramMap == null){
            paramMap = new HashMap();
        }
        paramMap.put("offset", offset);
        paramMap.put("pageSize", pageSize);
        return listBy("listAccountNoPage", paramMap);
    }
}
