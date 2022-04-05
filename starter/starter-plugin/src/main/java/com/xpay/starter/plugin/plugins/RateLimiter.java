package com.xpay.starter.plugin.plugins;

public interface RateLimiter {

    /**
     * 适用于令牌桶算法
     * @param name
     * @param replenishRate
     * @return
     */
    public boolean tryAcquire(String name, int replenishRate);

    /**
     * 适用于令牌桶算法
     * @param name
     * @param replenishRate
     * @param burstCapacity
     * @return
     */
    public boolean tryAcquire(String name, int replenishRate, int burstCapacity);

    public void destroy();
}
