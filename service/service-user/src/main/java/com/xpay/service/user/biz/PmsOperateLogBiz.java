package com.xpay.service.user.biz;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PmsOperateLogDto;
import com.xpay.service.user.dao.PmsOperateLogDao;
import com.xpay.service.user.entity.PmsOperateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 */
@Service
public class PmsOperateLogBiz {
    @Autowired
    private PmsOperateLogDao pmsOperateLogDao;

    public void createOperateLog(PmsOperateLogDto operateLog) {
        PmsOperateLog pmsOperateLog = BeanUtil.newAndCopy(operateLog, PmsOperateLog.class);
        pmsOperateLogDao.insert(pmsOperateLog);
    }

    public PageResult<List<PmsOperateLogDto>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<PmsOperateLog>> result = pmsOperateLogDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PmsOperateLogDto.class), result);
    }

    public PmsOperateLogDto getOperateLogById(Long id) {
        PmsOperateLog pmsOperateLog = pmsOperateLogDao.getById(id);
        return BeanUtil.newAndCopy(pmsOperateLog, PmsOperateLogDto.class);
    }

}
