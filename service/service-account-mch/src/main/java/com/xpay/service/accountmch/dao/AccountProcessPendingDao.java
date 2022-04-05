/*
 * Powered By [joinPay.com]
 */
package com.xpay.service.accountmch.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.utils.DateUtil;
import com.xpay.facade.accountmch.dto.PendingInfoDto;
import com.xpay.service.accountmch.entity.AccountProcessPending;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AccountProcessPendingDao extends MyBatisDao<AccountProcessPending, Long> {

    public boolean updatePendingStatus(Long id, AccountProcessPendingStageEnum stageNew, AccountProcessPendingStageEnum stageOld){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("stageNew", stageNew.getValue());
        paramMap.put("stageOld", stageOld.getValue());
        return update("updatePendingStatus", paramMap) > 0;
    }

    public boolean updatePendingStatus(List<Long> idList, AccountProcessPendingStageEnum stageNew, AccountProcessPendingStageEnum stageOld){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("idList", idList);
        paramMap.put("stageNew", stageNew.getValue());
        paramMap.put("stageOld", stageOld.getValue());
        return update("batchUpdatePendingStatus", paramMap) == idList.size();
    }

    public List<Long> listAccountProcessPendingId(Map<String, Object> paramMap, int offset, int limit) {
        paramMap.put("offset", offset);
        paramMap.put("limit", limit);
        return listBy("listAccountProcessPendingId", paramMap);
    }

    public List<PendingInfoDto> listAccountProcessPendingInfo(Map<String, Object> paramMap, int offset, int limit){
        paramMap.put("offset", offset);
        paramMap.put("limit", limit);
        return listBy("listAccountProcessPendingInfo", paramMap);
    }

    public int countProcessingTooLongRecord(List<Date> createDates, Integer processStage, Integer timeDiffSecond){
        List<String> createDateStrList = new ArrayList<>(createDates.size());
        createDates.forEach(date -> createDateStrList.add(DateUtil.formatDate(date)));

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("createDateList", createDateStrList);
        paramMap.put("processStage", processStage);
        paramMap.put("timeDiffSecond", timeDiffSecond);
        return (int) countBy("countProcessingTooLongRecord", paramMap);
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
