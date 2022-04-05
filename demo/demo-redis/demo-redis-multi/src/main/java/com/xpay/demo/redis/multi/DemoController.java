package com.xpay.demo.redis.multi;

import com.xpay.common.utils.JsonUtil;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.plugins.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试redis多数据源
 * 说明：
 * 1、默认数据源，即使用 spring.redis 作为配置前缀的数据源，此数据源用于 @Cacheable 注解、默认的RedisClient
 * 2、redis限流器单独一个数据源
 * 3、redis分布式锁单独一个数据源
 * 4、
 *
 *
 */
@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    RedisClient redisClient;
    @Autowired
    CacheService cacheService;

    @Autowired
    RateLimiter redisLimiter;
    @Autowired
    DistributedLock redisLock;

    @Autowired(required = false)
    @Qualifier("oneRedisClient")
    RedisClient oneRedisClient;
    @Autowired(required = false)
    @Qualifier("twoRedisClient")
    RedisClient twoRedisClient;
    @Autowired(required = false)
    @Qualifier("threeRedisClient")
    RedisClient threeRedisClient;

    @RequestMapping("defaultSet")
    public String defaultSet(String key, String value){
        redisClient.set(key, value, 20);
        return redisClient.get(key);
    }
    @RequestMapping("cache")
    public String cache(String name){
        Map<String, String> map = cacheService.getMap(name);
        return JsonUtil.toJson(map);
    }

    @RequestMapping("limit")
    public String limit(String name, int rate, int capacity){
        boolean isAllow = redisLimiter.tryAcquire(name, rate, capacity);
        return isAllow ? "PASS" : "LIMITED";
    }

    @RequestMapping("tryLock")
    public String tryLock(String lockName){
        int waitMills = 1000, expireMills = 80000;
        Object lock = redisLock.tryLock(lockName, waitMills, expireMills);
        if(lock == null){
            return "fail";
        }else{
            redisLock.unlock(lock);
            return "success";
        }
    }


    @RequestMapping("oneSet")
    public String oneSet(String key, String value){
        oneRedisClient.set(key, value, 20);
        return oneRedisClient.get(key);
    }

    @RequestMapping("twoSet")
    public String twoSet(String key, String value){
        twoRedisClient.set(key, value, 20);
        return twoRedisClient.get(key);
    }

    @RequestMapping("threeSet")
    public String threeSet(String key, String value){
        threeRedisClient.set(key, value, 20);
        return threeRedisClient.get(key);
    }
}
