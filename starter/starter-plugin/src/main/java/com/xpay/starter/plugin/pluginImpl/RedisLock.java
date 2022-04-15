package com.xpay.starter.plugin.pluginImpl;

import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.util.Utils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁，性能好，但可靠性没有zk分布式锁好，适合对可靠性要求较低，但是对性能/并发量要求较高的场景，如果即需要性能又需要可靠性，
 * 可跟数据库乐观锁联合使用，以规避在比较极端情况下出现的redis可靠性问题
 * @author chenyf
 */
public class RedisLock implements DistributedLock<RLock> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String lockNamePrefix = "lock:";
    private RedissonClient client;
    private ConcurrentHashMap<RLock, String> longLockMap = new ConcurrentHashMap<>();//在应用关闭前释放锁，避免因为当前应用重启导致其他客户端长时间无法获取到锁

    public RedisLock(RedissonClient client){
        if(client == null){
            throw new RuntimeException("RedissonClient 不能为null");
        }
        this.client = client;
    }

    public String getLockNamePrefix() {
        return lockNamePrefix;
    }

    public void setLockNamePrefix(String lockNamePrefix) {
        this.lockNamePrefix = lockNamePrefix;
    }

    public RedissonClient getClient(){
        return client;
    }

    /**
     * 获取锁，适合锁定时间短的业务场景
     * @param lockName             锁名称
     * @param waitMills            获取锁的等待时间
     * @param expireMills          锁的有效时间，单位(毫秒)，如果此值为-1，会分配一个默认时间，然后会自动不断地刷新锁的有效期
     * @return
     */
    @Override
    public RLock tryLock(String lockName, int waitMills, long expireMills){
        lockName = getRealLockName(lockName);
        RLock lock = getClient().getLock(lockName);
        try{
            if(lock.tryLock(waitMills, expireMills, TimeUnit.MILLISECONDS)){
                if(isLongLock(expireMills)){
                    longLockMap.put(lock, lockName);
                }

                return lock;
            }
        }catch(Throwable e){
            logger.error("lockName={} 获取锁时出现异常", lockName, e);
        }
        return null;
    }

    /**
     * 批量获取锁，要么全部获取成功，要么全部获取失败，适合锁定时间短的业务场景
     * @param lockNameList          锁名称
     * @param waitMills             获取锁的等待时间
     * @param expireMills           锁的有效时间，如果此值为-1，会分配一个默认时间，然后会自动不断地刷新锁的有效期
     * @return
     */
    @Override
    public List<RLock> tryLock(Set<String> lockNameList, int waitMills, long expireMills){
        List<RLock> lockList = new ArrayList<>(lockNameList.size());
        TimeUnit unit = TimeUnit.MILLISECONDS;
        //开始时间
        long startTime = System.currentTimeMillis();
        //剩余时间
        long remainTime = waitMills;
        //获取锁时的真正等待时间
        long awaitTime = 0;

        for(String lockName : lockNameList){
            try{
                lockName = getRealLockName(lockName);
                RLock lock = getClient().getLock(lockName);
                remainTime -= System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                awaitTime = Math.max(remainTime, 0);

                if(awaitTime <= 0){
                    logger.error("lockName={} lockNameList = {} 获取锁超时", lockName, Utils.toJson(lockNameList));
                    break;
                }else if(lock.tryLock(awaitTime, expireMills, unit)){
                    if(isLongLock(expireMills)){
                        longLockMap.put(lock, lockName);
                    }
                    lockList.add(lock);
                }else{
                    logger.error("lockName={} 获取锁失败", lockName);
                    break;
                }
            }catch(Throwable e){
                logger.error("lockName={} 获取锁时出现异常", lockName, e);
                break;
            }
        }

        //如果其中有任何一个账户获取锁失败，则全部锁释放
        if(lockList.size() != lockNameList.size()){
            unlock(lockList);
            //返回空List
            return new ArrayList<>();
        }
        return lockList;
    }

    /**
     * 批量释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请查看 {@link #forceUnlock(RLock)}
     * @param lockList
     * @return 如果全部解锁成功，则返回true，否则，返回false
     */
    @Override
    public void unlock(List<RLock> lockList){
        if(lockList == null || lockList.isEmpty()){
            return;
        }

        for(RLock lock : lockList){
            try{
                unlock(lock);
            }catch(Throwable t){
                logger.error("释放锁异常", t);
            }
        }
    }

    /**
     * 释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请查看 {@link #forceUnlock(RLock)}
     * @param lock
     * @return
     */
    @Override
    public void unlock(RLock lock) throws RuntimeException {
        try{
            if(longLockMap.get(lock) != null){
                longLockMap.remove(lock);
            }

            lock.unlock();
        }catch(Throwable t){
            throw new RuntimeException("lockName = "+lock.getName()+" 释放锁时出现异常", t);
        }
    }

    /**
     * 强行释放锁，不管释放锁的线程是不是跟加锁时的线程一样，都可以释放锁
     * @param lock
     * @return
     */
    @Override
    public void forceUnlock(RLock lock){
        try{
            if(longLockMap.get(lock) != null){
                longLockMap.remove(lock);
            }

            lock.forceUnlock();
        }catch(Throwable t){
            logger.error("lockName = {} 强制释放锁时出现异常", lock.getName(), t);
        }
    }

    /**
     * 在应用关闭前释放长时间锁
     */
    @Override
    public void destroy(){
        if(client != null){
            try {
                //waiting for rpc shutdown
                Thread.sleep(2000);
            } catch (Exception e){}

            for(Map.Entry<RLock, String> entry : longLockMap.entrySet()){
                logger.info("lockName={} 应用关闭前强制释放锁", entry.getValue());
                try {
                    entry.getKey().forceUnlock();
                } catch (Exception e) {
                }
            }
            //关闭客户端，第1个参数是静默等待时间，第2个参数是最大等待时间，第3个参数是时间单位
            client.shutdown(3, 7, TimeUnit.SECONDS);
        }
    }

    private String getRealLockName(String lockName){
        return getLockNamePrefix() + lockName;
    }

    private boolean isLongLock(long expireMills){
        return expireMills > 5 * 60 * 1000L;//大于5分钟就认为是长时间锁
    }
}
