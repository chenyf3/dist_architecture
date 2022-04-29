package com.xpay.service.message.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.utils.DateUtil;
import com.xpay.service.message.entity.MailDelayRecord;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MailDelayRecordDao extends MyBatisDao<MailDelayRecord, Long> {

    public List<MailDelayRecord> listPendingRecord(List<String> createDateList, Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("createDateList", createDateList);
        paramMap.put("createTimeEnd", DateUtil.formatDateTime(endTime));
        paramMap.put("maxSendTimes", maxSendTimes);
        return listBy("listPendingRecord", paramMap, "ID", offset, limit);
    }

    public List<MailDelayRecord> listSendingOvertimeRecord(List<String> createDateList, Date endTime, Integer offset, Integer limit, Integer maxSendTimes){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("createDateList", createDateList);
        paramMap.put("sendStartTimeEnd", DateUtil.formatDateTime(endTime));
        paramMap.put("maxSendTimes", maxSendTimes);
        return listBy("listSendingOvertimeRecord", paramMap, "ID", offset, limit);
    }

    public List<MailDelayRecord> listFinishOrOvertimesRecord(Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("createDateEnd", DateUtil.formatDate(endTime));
        paramMap.put("maxSendTimes", maxSendTimes);
        return listBy("listFinishOrOvertimesRecord", paramMap, "ID", offset, limit);
    }

    public int updatePendingToSending(List<Long> idList, Date sendStartTime){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("idList", idList);
        paramMap.put("sendStartTime", sendStartTime);
        return update("updatePendingToSending", paramMap);
    }

    public int revertSendingToPending(List<Long> idList){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("idList", idList);
        return update("revertSendingToPending", paramMap);
    }

    public int updateSendingToFinish(List<Long> idList, Date sendFinishTime){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("idList", idList);
        paramMap.put("sendFinishTime", sendFinishTime);
        return update("updateSendingToFinish", paramMap);
    }
}
