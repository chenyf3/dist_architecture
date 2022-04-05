package com.xpay.service.user.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PmsOperateLogDto;
import com.xpay.facade.user.service.PmsOperateLogFacade;
import com.xpay.service.user.biz.PmsOperateLogBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class PmsOperateLogFacadeImpl implements PmsOperateLogFacade {
    @Autowired
    PmsOperateLogBiz pmsOperateLogBiz;

    @Override
    public void createOperateLog(PmsOperateLogDto operateLog) {
        pmsOperateLogBiz.createOperateLog(operateLog);
    }

    @Override
    public PmsOperateLogDto getOperateLogById(Long id) {
        return pmsOperateLogBiz.getOperateLogById(id);
    }

    @Override
    public PageResult<List<PmsOperateLogDto>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return pmsOperateLogBiz.listOperateLogPage(paramMap, pageQuery);
    }
}
