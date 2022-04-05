/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.utils.DateUtil;
import com.xpay.service.accountmch.entity.AccountProcessResult;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AccountProcessResultDao extends MyBatisDao<AccountProcessResult, Long> {

    public List<Long> listAccountProcessResultId(Map<String, Object> paramMap, int offset, int limit) {
        paramMap.put("offset", offset);
        paramMap.put("limit", limit);
        return listBy("listAccountProcessResultId", paramMap);
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

}
