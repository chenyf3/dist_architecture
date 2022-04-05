package com.xpay.starter.plugin.plugins;

import java.util.List;
import java.util.Set;

public interface DistributedLock<T> {

    /**
     * 获取锁，适合锁定时间短的业务场景
     * @param lockName             锁名称
     * @param waitMills            获取锁的等待时间
     * @param expireMills          锁的有效时间，单位(毫秒)，如果此值为-1，客户端需要设置一个默认过期时间，之后需要自行续租，保证任务运行期间锁不会过期
     * @return
     */
    public T tryLock(String lockName, int waitMills, long expireMills);

    /**
     * 批量获取锁，要么全部获取成功，要么全部获取失败，适合锁定时间短的业务场景
     * @param lockNameList          锁名称
     * @param waitMills             获取锁的等待时间
     * @param expireMills           锁的有效时间，如果此值为-1，客户端需要设置一个默认过期时间，之后需要自行续租，保证任务运行期间锁不会过期
     * @return
     */
    public List<T> tryLock(Set<String> lockNameList, int waitMills, long expireMills);

    /**
     * 批量释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请使用 {@link #forceUnlock(T)}
     * @param lockList
     * @return 如果全部解锁成功，则返回true，否则，返回false
     */
    public void unlock(List<T> lockList);

    /**
     * 释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请使用 {@link #forceUnlock(T)}
     * @param lock
     * @return
     */
    public void unlock(T lock);

    /**
     * 强行释放锁，不管释放锁的线程是不是跟加锁时的线程一样，都可以释放锁
     * @param lock
     * @return
     */
    public void forceUnlock(T lock);

    /**
     * 在应用关闭前进行资源释放
     */
    public void destroy();
}
