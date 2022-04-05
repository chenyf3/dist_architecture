package com.xpay.common.statics.constants.common;

public class DistLockConst {
    /**
     * 是否采用数据库悲观锁
     */
    public final static boolean PESSIMIST_LOCK = false;
    //获取账务锁的等待时间
    public final static int ACCOUNT_LOCK_WAIT_MILLS = 20 * 1000;
    //账务锁的自动超时时间
    public final static long ACCOUNT_LOCK_EXPIRE_MILLS = 5 * 1000;

    public final static String ACCOUNT_MCH_LOCK_PREFIX = "accountMchLock-";
}
