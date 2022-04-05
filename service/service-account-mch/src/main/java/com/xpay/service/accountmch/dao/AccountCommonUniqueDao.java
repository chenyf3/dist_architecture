package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.accountmch.entity.AccountCommonUnique;
import com.xpay.common.utils.DateUtil;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountCommonUniqueDao extends MyBatisDao<AccountCommonUnique, Long> {

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

    public int deleteRecordByIdList(List<Long> detailIdList){
        Map<String, Object> param = new HashMap<>(8);
        param.put("recordIdList", detailIdList);
        return deleteBy("deleteRecordByIdList", param);
    }
}
