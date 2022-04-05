package com.xpay.service.config.biz;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.base.config.dto.DistLockDto;
import com.xpay.facade.base.config.enums.DistLockStatusEnum;
import com.xpay.service.config.dao.DistLockDao;
import com.xpay.service.config.entity.DistLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author chenyf
 */
@Component
public class DistLockBiz {
    private final static long NEVER_EXPIRE_TIMESTAMP_VALUE = -1;
    private Logger logger = LoggerFactory.getLogger(DistLockBiz.class);

    @Autowired
    private DistLockDao distLockDao;

    /**
     * 获取资源锁
     * @param resourceId   申请的资源ID
     * @param expireSecond 超时时间 -1表示永不超时
     * @param clientFlag   客户端标识
     * @return 锁ID
     */
    public String tryLock(String resourceId, int expireSecond, String clientFlag) {
        if (StringUtil.isEmpty(resourceId)) {
            throw new BizException(BizException.PARAM_INVALID, "resourceId不能为空");
        } else if (resourceId.length() > 64) {
            throw new BizException(BizException.PARAM_INVALID, "resourceId长度不能超过64");
        } else if (expireSecond <= 0 && expireSecond != NEVER_EXPIRE_TIMESTAMP_VALUE) {
            throw new BizException(BizException.PARAM_INVALID, "expireSecond 值无效");
        } else if (StringUtil.isNotEmpty(clientFlag) && clientFlag.length() > 100) {
            throw new BizException(BizException.PARAM_INVALID, "clientFlag 长度不能超过100");
        }

        DistLock lock = distLockDao.getByResourceId(resourceId);
        if (lock == null) { //如果还没有这条资源的记录，则新增一条记录并获得锁
            lock = new DistLock();
            lock.setResourceId(resourceId);
            return this.acquireLock(lock, expireSecond, clientFlag);
        } else if (lock.getResourceStatus() == DistLockStatusEnum.FREE.getValue()) {
            //此锁当前空闲，当前客户端可以正常获得锁
            return this.acquireLock(lock, expireSecond, clientFlag);
        } else if (lock.getExpireTime() == null) {
            //此锁锁定中，并且过期时间为NULL，说明锁持有者设置为永不过期，当前客户端不可获得锁
            return null;
        } else if (DateUtil.compare(lock.getExpireTime(), new Date(), Calendar.SECOND) > 0) {
            //此锁锁定中，并且过期时间大于当前时间，说明锁持有者持有的锁还未过期，当前客户端不可获得锁
            return null;
        } else {
            //此锁锁定中，并且过期时间小于当前时间，说明是锁持有者已过期，当前客户端可以正常获得锁
            return this.acquireLock(lock, expireSecond, clientFlag);
        }
    }

    /**
     * 解锁
     * @param resourceId 释放的资源ID
     * @param clientId   客户端获取得到的锁ID
     * @return .
     */
    public boolean unlock(String resourceId, String clientId) {
        if (StringUtil.isEmpty(resourceId)) {
            throw new BizException(BizException.PARAM_INVALID, "resourceId不能为空");
        } else if (StringUtil.isEmpty(clientId)) {
            throw new BizException(BizException.PARAM_INVALID, "clientId不能为空");
        }

        DistLock lock = distLockDao.getByResourceId(resourceId);
        if (lock == null) {
            return true;
        }

        if (lock.getResourceStatus().equals(DistLockStatusEnum.FREE.getValue())) {
            //此锁当前空闲，没必要再解锁，直接返回true
            return true;
        } else if (! clientId.equals(lock.getClientId())) {
            //此锁锁定中，但是传入的clientId跟数据库中的不相等，说明当前解锁者不是锁持有者，所以不能解锁
            return false;
        } else {
            //此锁锁定中，并且当前解锁者是锁持有者，可以释放锁
            return this.releaseLock(lock, clientId);
        }
    }

    /**
     * 强制解锁
     * @param resourceId   .
     * @param isNeedDelete .
     * @return .
     */
    public boolean unlockForce(String resourceId, boolean isNeedDelete) {
        if (StringUtil.isEmpty(resourceId)) {
            throw new BizException(BizException.PARAM_INVALID, "resourceId不能为空");
        }

        DistLock lock = distLockDao.getByResourceId(resourceId);
        if (lock == null) {
            return true;
        }

        if (isNeedDelete) {
            return deleteResourceLock(resourceId);
        } else {
            return this.releaseLock(lock, "");
        }
    }

    /**
     * 删除资源锁记录
     * @param resourceId .
     * @return .
     */
    public boolean deleteResourceLock(String resourceId) {
        if (StringUtil.isEmpty(resourceId)) {
            throw new BizException(BizException.PARAM_INVALID, "resourceId不能为空");
        }

        DistLock lock = distLockDao.getByResourceId(resourceId);
        if (lock == null) {
            return true;
        }
        try {
            distLockDao.deleteById(lock.getId());
            return true;
        } catch (Exception ex) {
            logger.info("删除锁时失败,resourceId={}", resourceId, ex);
            return false;
        }
    }

    /**
     * 分页获取数据
     *
     * @param paramMap  .
     * @param pageQuery .
     * @return .
     */
    public PageResult<List<DistLockDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<DistLock>> result = distLockDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), DistLockDto.class), result);
    }

    private String acquireLock(DistLock lock, int expireSecond, String clientFlag) {
        lock.setResourceStatus(DistLockStatusEnum.LOCKING.getValue());
        lock.setClientId(MD5Util.getMD5Hex(UUID.randomUUID().toString()));
        lock.setClientFlag(clientFlag);
        lock.setLockTime(new Date());
        if (expireSecond == NEVER_EXPIRE_TIMESTAMP_VALUE) {
            lock.setExpireTime(null);
        } else {
            lock.setExpireTime(DateUtil.addSecond(lock.getLockTime(), expireSecond));
        }

        try {
            if(lock.getId() == null){
                distLockDao.insert(lock);
            }else{
                distLockDao.update(lock); //使用version乐观锁来控制并发时的情况
            }
            return lock.getClientId();
        } catch (Throwable ex) {
            logger.error("获取锁时失败 resourceId={} clientFlag={}", lock.getResourceId(), clientFlag, ex);
        }
        return null;
    }

    private boolean releaseLock(DistLock lock, String clientId) {
        lock.setResourceStatus(DistLockStatusEnum.FREE.getValue());
        lock.setClientId("");
        lock.setClientFlag("");
        lock.setLockTime(null);
        lock.setExpireTime(null);

        try {
            distLockDao.update(lock);
            return true;
        } catch (Exception ex) {
            logger.error("释放锁过程中出现异常, resourceId={}, clientId={}", lock.getResourceId(), clientId);
            return false;
        }
    }
}
