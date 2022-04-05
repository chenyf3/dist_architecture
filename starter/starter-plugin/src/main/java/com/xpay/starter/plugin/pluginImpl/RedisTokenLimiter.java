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
        return tryAcquire(name, replenishRate, replenishRate+1);
    }

    /**
     * 获取令牌，成功返回true，失败返回false
     * 怎么理解replenishRate和burstCapacity这两个参数呢：一开始，令牌桶是满的，等于burstCapacity的值，然后，突然大流量涌进来，
     * 此时被放行的流量最大不会超过burstCapacity，如果大流量持续涌入，则被放行的流量最多也不会超过replenishRate的值
     *
     * @param name             限流的key值
     * @param replenishRate    桶填充速率(单位：个/秒)，就是允许用户每秒执行多少请求
     * @param burstCapacity    桶容量，一秒钟内允许执行的最大请求数。
     * @return
     */
    @Override
    public boolean tryAcquire(String name, int replenishRate, int burstCapacity){
        if(replenishRate > burstCapacity){
            replenishRate = burstCapacity;
        }
        List<String> keys = getKeys(name);
        List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "",
                Instant.now().getEpochSecond() + "", "1");

        String sha = shaCache.getIfPresent(name);
        if(sha == null){
            sha = redisClient.scriptLoad(LIMIT_SCRIPT);
            shaCache.put(name, sha);
            logger.info("script load success, sha = {}", sha);
        }
        List<Long> results = redisClient.evalsha(sha, keys, scriptArgs, List.class);
        boolean allowed = results != null && results.get(0) == 1L;
        return allowed;
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
