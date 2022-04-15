package com.xpay.starter.plugin.pluginImpl;

import com.xpay.starter.plugin.plugins.RateLimiter;
import com.xpay.starter.plugin.client.RedisClient;
import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Redis令牌桶限流器，令牌桶算法，以固定速率往桶中添加令牌，客户端瞬时最大可以获得桶容量的限流速率
 * @author chenyf
 */
public class RedisTokenLimiter implements RateLimiter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RedisClient redisClient;
    private Cache<String, String> shaCache;

    public RedisTokenLimiter(RedisClient redisClient, Cache<String, String> shaCache){
        this.redisClient = redisClient;
        this.shaCache = shaCache;
    }

    @Override
    public boolean tryAcquire(String name, int replenishRate){
        return tryAcquire(name, replenishRate, replenishRate + 1);
    }

    /**
     * 获取令牌，成功返回true，失败返回false
     * 一般情况下放行的流速不会超过replenishRate的值，因为这个值代表生产令牌的速率，但也可能存在一种情况是桶里面的令牌是满的，这个时候突然有大流量涌进来，
     * 此时被放行的流量最大不会超过burstCapacity，如果大流量持续涌入，则被放行的流量基本会维持在replenishRate这个值附近
     *
     * @param name             限流的key值
     * @param replenishRate    桶填充速率(单位：个/秒)，就是允许用户每秒执行多少请求
     * @param burstCapacity    桶容量，此参数也可理解为系统的瞬时最大承载量
     * @return
     */
    @Override
    public boolean tryAcquire(String name, int replenishRate, int burstCapacity){
        if(replenishRate > burstCapacity){
            replenishRate = burstCapacity;
        }

        String now = String.valueOf(Instant.now().getEpochSecond());//获取到秒，这个决定了 replenishRate 这个参数的单位就是 个/秒
        List<String> keys = getKeys(name);
        List<String> scriptArgs = Arrays.asList(String.valueOf(replenishRate), String.valueOf(burstCapacity), now, "1");

        String sha = shaCache.getIfPresent(name);
        if(sha == null){
            sha = redisClient.scriptLoad(LIMIT_SCRIPT);
            shaCache.put(name, sha);
            logger.info("script load success, sha = {}", sha);
        }
        List<Long> results = redisClient.evalsha(sha, keys, scriptArgs, List.class);
        return results != null && results.get(0) == 1L;
    }

    private List<String> getKeys(String id) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster

        // Make a unique key per user.
        String prefix = "token_rate_limiter.{" + id;

        // You need two Redis keys for Token Bucket.
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    @Override
    public void destroy() {
        if(redisClient != null){
            redisClient.destroy();
        }
    }

    /**
     * 令牌桶算法，来自spring-cloud-gateway项目下/META-INF/scripts/request_rate_limiter.lua
     * 核心思想：
     *  1、当前剩余令牌数量 = 上次取出后剩余的令牌数量 + (当前时间 - 上次取出时间) * 令牌生成速率，
     *  2、因为桶是有容量的，所以 当前剩余令牌数量 = min(桶的容量, 当前剩余令牌数量)
     *  3、如果 当前剩余令牌数量 是大于等于需要取出的数量则通过，否则发生限流
     *
     *  脚本有关解释说明如下：
     *    2个key：第1个key是存放当前剩余token数量，第2个key是存放上次获取token的时间戳
     *    4个参数：第1个是令牌发放速率(个/秒，个/毫秒，单位取决于传入的时间是秒还是毫秒)，第2个是令牌桶的容量，第3个是当前时间，第4个本次需要获取的令牌数量
     *    fill_time变量：脚本中的fill_time为容量除以速率，也就是桶中令牌填满需要花费的时间，然后以这个时间的2倍作为上面两个key的失效时间
     *    last_tokens变量：脚本中last_tokens为当前剩余的token数量
     *    last_refreshed变量：脚本中last_refreshed为上次获取token的时间
     */
    public final static String LIMIT_SCRIPT = "" +
            "local tokens_key = KEYS[1];" +
            "local timestamp_key = KEYS[2];" +

            "local rate = tonumber(ARGV[1]);" +
            "local capacity = tonumber(ARGV[2]);" +
            "local now = tonumber(ARGV[3]);" +
            "local requested = tonumber(ARGV[4]);" +

            "local fill_time = capacity/rate;" +
            "local ttl = math.floor(fill_time*2);" +

            "local last_tokens = tonumber(redis.call('get', tokens_key));" +
            "if last_tokens == nil then " +
            "  last_tokens = capacity;" +
            "end;" +

            "local last_refreshed = tonumber(redis.call('get', timestamp_key));" +
            "if last_refreshed == nil then " +
            "  last_refreshed = 0;" +
            "end;" +

            "local delta = math.max(0, now-last_refreshed);" +
            "local filled_tokens = math.min(capacity, last_tokens+(delta*rate));" +
            "local allowed = filled_tokens >= requested;" +
            "local new_tokens = filled_tokens;" +
            "local allowed_num = 0;" +
            "if allowed then " +
            "  new_tokens = filled_tokens - requested;" +
            "  allowed_num = 1;" +
            "end;" +

            "redis.call('setex', tokens_key, ttl, new_tokens);" +
            "redis.call('setex', timestamp_key, ttl, now);" +

            "return { allowed_num, new_tokens };";
}
