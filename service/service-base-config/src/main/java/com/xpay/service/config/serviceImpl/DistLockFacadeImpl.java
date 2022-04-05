package com.xpay.service.config.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.base.config.dto.DistLockDto;
import com.xpay.facade.base.config.service.DistLockFacade;
import com.xpay.service.config.biz.DistLockBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class DistLockFacadeImpl implements DistLockFacade {
    @Autowired
    private DistLockBiz distLockBiz;

    @Override
    public String tryLock(String resourceId, int expireSecond, String clientFlag) {
        return distLockBiz.tryLock(resourceId, expireSecond, clientFlag);
    }

    @Override
    public boolean unlock(String resourceId, String clientId) {
        return distLockBiz.unlock(resourceId, clientId);
    }

    @Override
    public boolean unlockForce(String resourceId, boolean isNeedDelete) {
        return distLockBiz.unlockForce(resourceId, isNeedDelete);
    }

    @Override
    public PageResult<List<DistLockDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return distLockBiz.listPage(paramMap, pageQuery);
    }
}
