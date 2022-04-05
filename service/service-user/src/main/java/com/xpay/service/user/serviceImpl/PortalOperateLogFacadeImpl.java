package com.xpay.service.user.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PortalOperateLogDto;
import com.xpay.facade.user.service.PortalOperateLogFacade;
import com.xpay.service.user.biz.PortalOperateLogBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class PortalOperateLogFacadeImpl implements PortalOperateLogFacade {
    @Autowired
    PortalOperateLogBiz operateLogBiz;

    @Override
    public void createOperateLog(PortalOperateLogDto operateLog) throws BizException {
        operateLogBiz.createOperateLog(operateLog);
    }

    @Override
    public PageResult<List<PortalOperateLogDto>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return operateLogBiz.listOperateLogPage(paramMap, pageQuery);
    }

    @Override
    public PortalOperateLogDto getOperateLogById(Long id) {
        return operateLogBiz.getOperateLogById(id);
    }
}
