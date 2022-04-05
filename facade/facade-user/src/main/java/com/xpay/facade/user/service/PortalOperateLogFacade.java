package com.xpay.facade.user.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PortalOperateLogDto;

import java.util.List;
import java.util.Map;

/**
 * Author: Cmf
 * Date: 2019/11/1
 * Time: 15:29
 * Description:
 */
public interface PortalOperateLogFacade {

    void createOperateLog(PortalOperateLogDto operateLog) throws BizException;

    PageResult<List<PortalOperateLogDto>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery);

    PortalOperateLogDto getOperateLogById(Long id);
}
