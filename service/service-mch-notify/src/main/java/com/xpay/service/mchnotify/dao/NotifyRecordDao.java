package com.xpay.service.mchnotify.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.mchnotify.entity.NotifyRecord;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class NotifyRecordDao extends MyBatisDao<NotifyRecord, Long> {

    public boolean appendNotifyLog(Long id, int status, String notifyLogJson){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("status", status);
        paramMap.put("notifyLog", notifyLogJson);
        paramMap.put("id", id);
        return update("appendNotifyLog", paramMap) > 0;
    }
}
