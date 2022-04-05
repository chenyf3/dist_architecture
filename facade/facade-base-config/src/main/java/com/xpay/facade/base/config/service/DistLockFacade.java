package com.xpay.facade.base.config.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.base.config.dto.DistLockDto;

import java.util.List;
import java.util.Map;

/**
 * 用数据库实现的分布式锁
 * 从微服务划分上来看的，这个接口应该是属于功能类而非配置类，但因为其本身适用场景有限，如果独立出一个专门的service又显浪费资源，所以放到本服务下
 */
public interface DistLockFacade {

    /**
     * @param resourceId   资源id，一个资源的唯一标志符
     * @param expireSecond 超时时间(秒)，超过时间后将自动释放锁，避免死锁，当为-1时表示永不超时，但如果客户端没有释放锁则会造成死锁
     * @param clientFlag   客户端标识，可为空值，只用以标识哪个客户端持有了锁，方便问题排查，如：IP + 应用名 ，便可以标识出是哪台服务器的哪个应用获得了锁
     * @return clientId 如果获取锁成功，会返回一个32位长度的非空字符串，如果获取锁失败，返回null
     * @description 尝试获得锁，如果得到了锁，会返回一个字符串，视为clientId，解锁的时候就需要传入此标识，如果获取不到锁，就返回null
     */
    String tryLock(String resourceId, int expireSecond, String clientFlag) throws BizException;

    /**
     * @description 释放锁，释放成功时返回true，释放失败时返回false
     * @param resourceId
     * @param clientId
     * @return
     * @see #tryLock(String, int, String)
     */
    boolean unlock(String resourceId, String clientId) throws BizException;

    /**
     * @param resourceId   .
     * @param isNeedDelete 释放锁时是否需要删除资源记录
     * @return
     * @description 强行释放锁，不管锁是不是正在被持有，是否超时，都强行释放，主要是可以对一些已经查明的死锁进行解锁
     */
    boolean unlockForce(String resourceId, boolean isNeedDelete) throws BizException;

    /**
     * 分页获取数据
     * @param paramMap
     * @param pageQuery
     * @return
     */
    PageResult<List<DistLockDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) throws BizException;
}
