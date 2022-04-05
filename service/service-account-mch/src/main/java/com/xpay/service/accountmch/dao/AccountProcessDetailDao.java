/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.dto.account.AccountDetailDto;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.service.accountmch.entity.AccountCommonUnique;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AccountProcessDetailDao extends MyBatisDao<AccountProcessDetail, Long> {
    @Autowired
    AccountCommonUniqueDao accountCommonUniqueDao;

    public void uniqueInsert(AccountProcessDetail accountDetail){
        AccountCommonUnique unique = buildAccountCommonUnique(accountDetail);
        accountCommonUniqueDao.insert(unique);
        super.insert(accountDetail);
    }

    public void uniqueInsert(List<AccountProcessDetail> accountDetailList){
        List<AccountCommonUnique> uniqueList = new ArrayList<>(accountDetailList.size());
        for(AccountProcessDetail accountDetail : accountDetailList){
            AccountCommonUnique unique = buildAccountCommonUnique(accountDetail);
            uniqueList.add(unique);
        }
        accountCommonUniqueDao.insert(uniqueList);
        super.insert(accountDetailList);
    }

    public boolean isAccountProcessDetailExist(String accountNo, String requestNo, Integer processType, String processNo){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("accountNo", accountNo);
        paramMap.put("requestNo", requestNo);
        paramMap.put("processType", processType);
        paramMap.put("processNo", processNo);
        return getOne(paramMap) != null;
    }

    public List<AccountDetailDto> listDetailDtoByAccountNoAndRequestNo(String accountNo, String requestNo){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("accountNo", accountNo);
        paramMap.put("requestNo", requestNo);
        return listBy("listDetailDtoByAccountNoAndRequestNo", paramMap);
    }

    public List<Date> listNeedMigrateDates(Date migrationEndDate){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("migrationEndDate", DateUtil.formatDate(migrationEndDate));
        return listBy("listNeedMigrateDates", paramMap);
    }

    public List<Long> listIdsForMigration(Date currMigrateDate, Integer migrateNumPerTime){
        Map<String, Object> param = new HashMap<>(8);
        param.put("currMigrateDate", currMigrateDate);
        param.put("migrateNumPerTime", migrateNumPerTime);
        return getSqlSession().selectList(fillSqlId("listIdsForMigration"), param);
    }

    public int migrateToHistoryByIds(List<Long> idList){
        Map<String, Object> param = new HashMap<>(8);
        param.put("recordIdList", idList);
        return insert("migrateToHistoryByIds", param);
    }

    public int deleteRecordByIdList(List<Long> detailIdList){
        Map<String, Object> param = new HashMap<>(8);
        param.put("recordIdList", detailIdList);
        return deleteBy("deleteRecordByIdList", param);
    }

    private AccountCommonUnique buildAccountCommonUnique(AccountProcessDetail accountDetail){
        StringBuilder sb = new StringBuilder();
        sb.append("ad_")
                .append(accountDetail.getAccountNo())
                .append(accountDetail.getRequestNo())
                .append(accountDetail.getProcessType());

        AccountCommonUnique unique = new AccountCommonUnique();
        unique.setUniqueKey(MD5Util.getMD5Hex(sb.toString()));
        unique.setCreateDate(new Date());
        return unique;
    }
}
