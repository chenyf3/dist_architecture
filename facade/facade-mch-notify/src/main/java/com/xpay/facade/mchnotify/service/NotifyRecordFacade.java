package com.xpay.facade.mchnotify.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.mchnotify.dto.NotifyRecordDto;

import java.util.List;
import java.util.Map;

public interface NotifyRecordFacade {

    public PageResult<List<NotifyRecordDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery);

    public NotifyRecordDto getNotifyRecord(Long id);

    public boolean notifyAgain(Long id, String operator);
}
