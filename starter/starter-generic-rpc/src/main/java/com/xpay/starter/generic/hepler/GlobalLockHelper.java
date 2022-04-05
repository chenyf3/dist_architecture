package com.xpay.starter.generic.hepler;

import com.xpay.starter.generic.service.BaseService;
import com.xpay.starter.generic.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chenyf
 * Date: 2020.1.17
 * Time: 14:42
 * Description: 全局锁帮助类
 * 此类基于数据库记录来实际全局锁，主要适用于并发性要求不高，但对锁的的稳定性要求比较高的场合（比如定时任务上锁）
 */
public class GlobalLockHelper {
    private Logger logger = LoggerFactory.getLogger(GlobalLockHelper.class);
    private ConcurrentHashMap<String, String> resourceIdMap = new ConcurrentHashMap<>();
    private BaseService baseService;

    public GlobalLockHelper(BaseService baseService){
        this.baseService = baseService;
    }

    /**
     * 为资源加上分布式锁，确保不会并发执行
     *
     * @param resourceId   资源ID
     * @param expireSecond 锁过期时间
     * @param clientFlag   客户端标识
     * @return 获取得到的锁ID，解锁时需要用此id
     */
    public String tryLock(String resourceId, int expireSecond, String clientFlag) {
        logger.info("尝试获取全局锁 resourceId={} expireSecond={} clientFlag={}", resourceId, expireSecond, clientFlag);
        String clientId = null;
        try {
            clientId = baseService.tryLock(resourceId, expireSecond, clientFlag);
            if (Util.isNotEmpty(clientId)) {
                logger.info("获取全局锁成功 resourceId={} clientId={} ", resourceId, clientId);
                resourceIdMap.put(resourceId, clientId);
            }
        } catch (Throwable e) {
            logger.error("resourceId={} 获取锁时出现异常", resourceId, e);
            this.sendEmail(resourceId, clientFlag, "获取锁时出现异常: " + e.getMessage());
        }
        return clientId;
    }

    /**
     * 为资源释放锁
     *
     * @param resourceId 资源id
     * @param clientId   获取锁时得到的锁ID
     * @return .
     */
    public boolean unlock(String resourceId, String clientId) {
        logger.info("释放全局锁 resourceId={} clientId={}", resourceId, clientId);
        int curTimes = 0;
        int maxTimes = 3;
        boolean isSuccess = false;
        do{
            try {
                isSuccess = baseService.unlock(resourceId, clientId);
            } catch (Throwable e) {
                logger.error("全局锁解锁时发生异常 resourceId={} clientId={} curTimes={}", resourceId, clientId, curTimes, e);
            }
        }while( !isSuccess && curTimes++ < maxTimes );

        if (! isSuccess) {
            try{
                this.sendEmail(resourceId, clientId, "释放锁失败");
            }catch(Exception e){}
        }
        resourceIdMap.remove(resourceId);
        return isSuccess;
    }

    /**
     * 发送预警邮件
     *
     * @param resourceId .
     * @param clientId   .
     * @param msg        .
     */
    private void sendEmail(String resourceId, String clientId, String msg) {
        StringBuffer content = new StringBuffer();
        content.append("resourceId=").append(resourceId)
                .append(",clientId=").append(clientId)
                .append(",message=").append(msg);
        baseService.sendMailAsync("DIST_LOCK_ALERT_GROUP", "同步锁操作预警", content.toString());
    }

    /**
     * 对象销毁前先解锁，避免应用重启时造成死锁
     */
    public void destroy() {
        for (Map.Entry<String, String> entry : resourceIdMap.entrySet()) {
            this.unlock(entry.getKey(), entry.getValue());
        }
    }
}
