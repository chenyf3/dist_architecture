package com.xpay.facade.user.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PmsOperateLogDto;

import java.util.List;
import java.util.Map;

public interface PmsOperateLogFacade {
    public void createOperateLog(PmsOperateLogDto operateLog);

    public PmsOperateLogDto getOperateLogById(Long id);

    public PageResult<List<PmsOperateLogDto>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery);
}
