package com.xpay.service.mchnotify.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.mchnotify.dto.NotifyRecordDto;
import com.xpay.facade.mchnotify.service.NotifyRecordFacade;
import com.xpay.service.mchnotify.biz.NotifyBiz;
import com.xpay.service.mchnotify.biz.NotifyRecordBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class NotifyRecordFacadeImpl implements NotifyRecordFacade {
    @Autowired
    NotifyRecordBiz notifyRecordBiz;
    @Autowired
    NotifyBiz notifyBiz;

    @Override
    public PageResult<List<NotifyRecordDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return notifyRecordBiz.listPage(paramMap, pageQuery);
    }

    @Override
    public NotifyRecordDto getNotifyRecord(Long id) {
        return notifyRecordBiz.getNotifyRecord(id);
    }

    @Override
    public boolean notifyAgain(Long id, String operator) {
        return notifyBiz.doNotify(id, operator);
    }
}
