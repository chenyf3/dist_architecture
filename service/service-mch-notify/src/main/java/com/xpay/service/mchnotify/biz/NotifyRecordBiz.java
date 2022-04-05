package com.xpay.service.mchnotify.biz;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.mchnotify.dto.NotifyRecordDto;
import com.xpay.facade.mchnotify.dto.NotifyLogDto;
import com.xpay.common.statics.enums.merchant.NotifyRecordStatusEnum;
import com.xpay.service.mchnotify.dao.NotifyRecordDao;
import com.xpay.service.mchnotify.entity.NotifyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NotifyRecordBiz {
    @Autowired
    NotifyRecordDao notifyRecordDao;

    public void insertOrAppendLog(NotifyRecordDto notifyRecord){
        if(notifyRecord.getId() != null) {
            NotifyRecord record = notifyRecordDao.getById(notifyRecord.getId());
            if(record != null){
                Integer recordStatus = record.getStatus();
                if(recordStatus == NotifyRecordStatusEnum.FAIL.getValue()){ //当前是失败状态，有可能需要更新为成功状态
                    recordStatus = notifyRecord.getStatus();
                }
                List<NotifyLogDto> logList = JsonUtil.toList(record.getNotifyLogs(), NotifyLogDto.class);
                logList.add(notifyRecord.getLog());
                record.setCurrTimes(record.getCurrTimes() + 1);
                record.setStatus(recordStatus);
                record.setNotifyLogs(JsonUtil.toJson(logList));
                notifyRecordDao.updateIfNotNull(record);
                return;
            }
        }

        NotifyLogDto log = notifyRecord.getLog();
        List<NotifyLogDto> logList = new ArrayList<>();
        logList.add(log);
        notifyRecord.setNotifyLogs(JsonUtil.toJson(logList));
        notifyRecordDao.insert(BeanUtil.newAndCopy(notifyRecord, NotifyRecord.class));
    }

    public PageResult<List<NotifyRecordDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<NotifyRecord>> result = notifyRecordDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), NotifyRecordDto.class), result);
    }

    public NotifyRecordDto getNotifyRecord(Long id) {
        NotifyRecord record = notifyRecordDao.getById(id);
        return BeanUtil.newAndCopy(record, NotifyRecordDto.class);
    }
}

