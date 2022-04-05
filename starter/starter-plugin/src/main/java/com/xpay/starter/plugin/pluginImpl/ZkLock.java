package com.xpay.starter.plugin.pluginImpl;

import com.xpay.starter.plugin.client.ZKClient;
import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.util.Utils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper分布式锁，zk分布式锁在zk集群下，其可靠性比redis分布式锁要高，但其性能没有redis分布式锁高，适合对可靠性要求高但对性能/并发量要求不高的情况
 * 使用此锁，zk服务端和客户端的版本最好都在3.5.4以上，否则可能出现临时节点不被释放的情况，详情参考：https://www.cnblogs.com/xiaodu1993/articles/xiaodu1993.html
 * @author chenyf
 */
public class ZkLock implements DistributedLock<InterProcessMutex> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String lockRootPath = "/lock";
    private ZKClient client;

    public ZkLock(ZKClient zkClient){
        if(zkClient == null){
            throw new RuntimeException("zkClient 不能为null");
        }
        this.client = zkClient;
    }

    public String getLockRootPath() {
        return lockRootPath;
    }

    public ZKClient getClient(){
        return client;
    }

    /**
     * 尝试获取锁
     * @param lockName             锁名称
     * @param waitMills            获取锁的等待时间
     * @param expireMills          锁的有效时间，单位(毫秒)，在zookeeper锁中无需此参数，因为，若zk的session连接断开，锁自动失效
     * @return
     */
    @Override
    public InterProcessMutex tryLock(String lockName, int waitMills, long expireMills) {
        lockName = paddingPath(lockName);
        InterProcessMutex lock = getClient().getReentrantLock(lockName);

        try{
            boolean isSuccess = lock.acquire(waitMills, TimeUnit.MILLISECONDS);
            return isSuccess ? lock : null;
        }catch(Exception e){
            logger.error("lockName={} 获取锁时出现异常", lockName, e);
        }
        return null;
    }

    /**
     * 批量获取锁，要么全部获取成功，要么全部获取失败
     * @param lockNameList          锁名称
     * @param waitMills             获取锁的等待时间
     * @param expireMills           锁的有效时间，单位(毫秒)，在zookeeper锁中无需此参数，因为，若zk的session连接断开，锁自动失效
     * @return
     */
    @Override
    public List<InterProcessMutex> tryLock(Set<String> lockNameList, int waitMills, long expireMills) {
        List<InterProcessMutex> lockList = new ArrayList<>(lockNameList.size());
        TimeUnit unit = TimeUnit.MILLISECONDS;
        //开始时间
        long startTime = System.currentTimeMillis();
        //剩余时间
        long remainTime = waitMills;
        //获取锁时的真正等待时间
        long awaitTime = 0;

        for(String lockName : lockNameList){
            try{
                lockName = paddingPath(lockName);
                InterProcessMutex lock = getClient().getReentrantLock(lockName);
                remainTime -= System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                awaitTime = Math.max(remainTime, 0);

                if(awaitTime <= 0){
                    logger.error("lockName={} lockNameList = {} 获取锁超时", lockName, Utils.toJson(lockNameList));
                    break;
                }else if(lock.acquire(awaitTime, unit)){
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
     * 批量释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请使用 {@link #forceUnlock(InterProcessMutex)}
     * @param lockList
     * @return 如果全部解锁成功，则返回true，否则，返回false
     */
    @Override
    public void unlock(List<InterProcessMutex> lockList) {
        if(lockList == null || lockList.isEmpty()){
            return;
        }

        for(InterProcessMutex lock : lockList){
            try{
                unlock(lock);
            }catch(Throwable t){
                logger.error("释放锁异常", t);
            }
        }
    }

    /**
     * 释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请使用 {@link #forceUnlock(InterProcessMutex)}
     * @param lock
     * @return
     */
    @Override
    public void unlock(InterProcessMutex lock) {
        try{
            lock.release();
        }catch(Throwable t){
            logger.error("释放锁时出现异常", t);
            throw new RuntimeException("lockName = " + getLockPath(lock) + " 释放锁时出现异常", t);
        }
    }

    /**
     * 强行释放锁，不管释放锁的线程是不是跟加锁时的线程一样，都可以释放锁
     * @param lock
     * @return
     */
    @Override
    public void forceUnlock(InterProcessMutex lock) {
        if(lock.isOwnedByCurrentThread()){
            unlock(lock);
        }
        throw new RuntimeException("Not Support");//zk不支持强制释放锁，加锁和释放锁的必须是同一个线程才可以
    }

    @Override
    public void destroy() {
        if(client != null){
            try{
                //waiting for rpc shutdown
                Thread.sleep(2500);
            }catch(Exception e){}

            client.destroy();
        }
    }

    private String getLockPath(InterProcessMutex lock) {
        try{
            return lock.getParticipantNodes().iterator().next();
        }catch(Exception e){
            logger.error("获取锁路径时出现异常", e);
            return null;
        }
    }

    private String paddingPath(String path){
        if(! path.startsWith("/")){
            path = "/" + path;
        }
        return lockRootPath + path;
    }
}
