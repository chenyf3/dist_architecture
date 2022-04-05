package com.xpay.demo.redis.single;

import com.xpay.common.utils.JsonUtil;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.plugins.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    RateLimiter rateLimiter;
    @Autowired
    RedisClient redisClient;
    @Autowired
    DistributedLock redisLock;
    @Autowired
    CacheService cacheService;

    @RequestMapping("limit")
    public String limit(String name, int rate, int capacity){
        boolean isAllow = rateLimiter.tryAcquire(name, rate, capacity);
        return isAllow ? "PASS" : "LIMITED";
    }

    @RequestMapping("set")
    public boolean set(String key, String value){
        return redisClient.set(key, value, 20);
    }

    /**
     * write adn read tps
     * @param key
     * @return
     * @throws Exception
     */
    @RequestMapping("wrTps")
    public String wrTps(String key, String value, Boolean isGet) throws Exception {
        int totalCount = 5000;
        AtomicInteger currCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger();
        int threadCount = 4;

        long start = System.currentTimeMillis();
        CountDownLatch countDown = new CountDownLatch(threadCount);
        for(int i=0; i<threadCount; i++){
            new Thread(() -> {
                while(currCount.incrementAndGet() <= totalCount){
                    try {
                        boolean isOk = redisClient.set(key, value);
                        if(isOk && (isGet != null && isGet)){
                            redisClient.get(key);
                        }else{
                            failCount.incrementAndGet();
                        }
                    } catch (Exception e){
                        failCount.incrementAndGet();
                        e.printStackTrace();
                    }
                }
                countDown.countDown();
            }).start();
        }

        countDown.await();
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);
        int tps = (int)(totalCount / costSec);

        return "wrTps, costSec = " + costSec + ", totalCount = " + totalCount + ", tps = " + tps + ", failCount = " + failCount;
    }

    /**
     * 测试持久化保存
     * @param key
     * @param value
     * @return
     */
    @RequestMapping("save")
    public boolean save(String key, String value){
        return redisClient.set(key, value);
    }

    @RequestMapping("get")
    public String get(String key){
        return redisClient.get(key);
    }

    @RequestMapping("hset")
    public Boolean hset(String key, String field, String value){
        return redisClient.hset(key, field, value);
    }

    @RequestMapping("hget")
    public String hget(String key, String field){
        return redisClient.hget(key, field);
    }

    @RequestMapping("cache")
    public String cache(String name){
        Map<String, String> map = cacheService.getMap(name);
        return JsonUtil.toJson(map);
    }

    @RequestMapping("cache2")
    public String cache2(String name){
        Map<String, String> map = cacheService.getMap2(name);
        return JsonUtil.toJson(map);
    }

    @RequestMapping("del")
    public Boolean del(String key){
        return redisClient.del(key);
    }

    @RequestMapping("hdel")
    public Boolean hdel(String key, String field){
        return redisClient.hdel(key, field);
    }

    @RequestMapping("tryLock")
    public String tryLock(String lockName){
        int waitMills = 1000, expireMills = 80000;
        Object lock = redisLock.tryLock(lockName, waitMills, expireMills);
        if(lock == null){
            return "fail";
        }

        try {
            try {
                //测试锁重入，如果没有冲入锁的情况，此处应该是处理业务逻辑
                redisLock.tryLock(lockName, waitMills, expireMills);
            } finally {
                redisLock.unlock(lock);
            }
        } finally {
            redisLock.unlock(lock);
        }
        return "success";
    }

    @RequestMapping("lockTps")
    public String lockTps(String lockName) throws Exception {
        int totalCount = 5000;
        AtomicInteger currCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger();
        int threadCount = 4;

        long start = System.currentTimeMillis();
        CountDownLatch countDown = new CountDownLatch(threadCount);
        for(int i=0; i<threadCount; i++){
            new Thread(() -> {
                while(currCount.incrementAndGet() <= totalCount){
                    try {
                        int waitMills = 1000, expireMills = 80000;
                        Object lock = redisLock.tryLock(lockName, waitMills, expireMills);
                        if(lock == null){
                            failCount.incrementAndGet();
                        }else{
                            try{
                                redisLock.unlock(lock);
                            }catch (Exception e){
                            }
                        }
                    } catch (Exception e){
                        failCount.incrementAndGet();
                        e.printStackTrace();
                    }
                }
                countDown.countDown();
            }).start();
        }

        countDown.await();
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);
        int tps = (int)(totalCount / costSec);

        return "lockTps, costSec = " + costSec + ", totalCount = " + totalCount + ", tps = " + tps + ", failCount = " + failCount;
    }
}
