package com.xpay.service.user.biz;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PortalOperateLogDto;
import com.xpay.service.user.dao.PortalOperateLogDao;
import com.xpay.service.user.entity.PortalOperateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class PortalOperateLogBiz {
    @Autowired
    private PortalOperateLogDao portalOperateLogDao;

    public void createOperateLog(PortalOperateLogDto operateLog) {
        PortalOperateLog portalOperateLog = BeanUtil.newAndCopy(operateLog, PortalOperateLog.class);
        portalOperateLogDao.insert(portalOperateLog);
    }

    public PageResult<List<PortalOperateLogDto>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<PortalOperateLog>> result = portalOperateLogDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PortalOperateLogDto.class), result);
    }

    public PortalOperateLogDto getOperateLogById(Long id) {
        PortalOperateLog log = portalOperateLogDao.getById(id);
        return BeanUtil.newAndCopy(log, PortalOperateLogDto.class);
    }
}
