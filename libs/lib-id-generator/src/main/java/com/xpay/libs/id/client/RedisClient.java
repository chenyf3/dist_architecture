package com.xpay.libs.id.client;

import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.RedisProperties;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.util.*;

/**
 * redis客户端
 * @author chenyf
 */
public class RedisClient {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String STATUS_OK = "OK";
    private final static int STAND_ALONE_MODE = 1;
    private final static int SENTINEL_MODE = 2;
    private final static int CLUSTER_MODE = 3;
    private int mode;
    private Pool<Jedis> jedisPool;
    private JedisCluster jedisCluster;

    public RedisClient(RedisProperties redisProperties){
        init(redisProperties);
    }

    public String get(String key){
        if(isClusterMode()){
            return getJedisCluster().get(key);
        }else{
            Jedis jedis = getJedis();
            try {
                return jedis.get(key);
            } finally {
                jedis.close();
            }
        }
    }

    public <T> T get(String key, Class<T> clz){
        if(isClusterMode()){
            return Utils.jsonToBean(getJedisCluster().get(key), clz);
        }else{
            Jedis jedis = getJedis();
            try {
                return Utils.jsonToBean(jedis.get(key), clz);
            } finally {
                jedis.close();
            }
        }
    }

    public boolean setnx(String key, String value){
        if(isClusterMode()){
            return STATUS_OK.equals(getJedisCluster().setnx(key, value));
        }else{
            Jedis jedis = getJedis();
            try {
                return STATUS_OK.equals(jedis.set(key, value));
            } finally {
                jedis.close();
            }
        }
    }

    public String hget(String key, String field){
        if(isClusterMode()){
            return getJedisCluster().hget(key, field);
        }else{
            Jedis jedis = getJedis();
            try {
                return jedis.hget(key, field);
            } finally {
                jedis.close();
            }
        }
    }

    public boolean hset(String key, String field, String value){
        if(isClusterMode()){
            return getJedisCluster().hset(key, field, value) >= 0;
        }else{
            Jedis jedis = getJedis();
            try {
                return jedis.hset(key, field, value) >= 0;
            } finally {
                jedis.close();
            }
        }
    }

    public Long hincr(String key, String field){
        if(isClusterMode()){
            return getJedisCluster().hincrBy(key, field, 1);
        }else{
            Jedis jedis = getJedis();
            try {
                return jedis.hincrBy(key, field, 1);
            } finally {
                jedis.close();
            }
        }
    }

    public Long incr(String key){
        if(isClusterMode()){
            return getJedisCluster().incr(key);
        }else{
            Jedis jedis = getJedis();
            try {
                return jedis.incr(key);
            } finally {
                jedis.close();
            }
        }
    }

    public <T> T eval(String script, List<String> keys, List<String> args){
        if(isClusterMode()){
            return (T) getJedisCluster().eval(script, keys, args);
        }else{
            Jedis jedis = getJedis();
            try {
                return (T) jedis.eval(script, keys, args);
            } finally {
                jedis.close();
            }
        }
    }

    public long loopHIncrId(String key, String field, int incrStep, long maxValue){
        if(incrStep > maxValue){
            throw new RuntimeException("incrStep = " + incrStep + ", cannot bigger than maxValue = " + maxValue);
        }

        if(isClusterMode()){
            Long id = getJedisCluster().hincrBy(key, field, incrStep);
            if(id > maxValue){
                id = resetLoopNum(key, field, incrStep, maxValue);
            }
            return id;
        }else{
            Jedis jedis = getJedis();
            try {
                Long id = jedis.hincrBy(key, field, incrStep);
                if(id > maxValue){
                    id = resetLoopNum(key, field, incrStep, maxValue);
                }
                return id;
            } finally {
                jedis.close();
            }
        }
    }

    public Long resetLoopNum(String key, String field, int incrStep, long maxValue){
        String script = "" +
                "local maxValue = tonumber(ARGV[3]);" +
                "local currValue = tonumber(redis.call('hincrby', KEYS[1], ARGV[1], ARGV[2]));" +
                "if currValue > maxValue then " +
                "  redis.call('hset', KEYS[1], ARGV[1], 0);" +
                "  currValue = tonumber(redis.call('hincrby', KEYS[1], ARGV[1], ARGV[2]));" +
                "end;" +
                "return currValue;";
        List<String> args = Arrays.asList(field, incrStep+"", maxValue+"");
        Long id = eval(script, Collections.singletonList(key), args);
        return id;
    }

    public void destroy(){
        if(jedisPool != null){
            jedisPool.destroy();
        }
        if(jedisCluster != null){
            jedisCluster.close();
        }
    }

    private boolean isClusterMode(){
        return mode == CLUSTER_MODE;
    }

    private Jedis getJedis(){
        return jedisPool.getResource();
    }

    private JedisCluster getJedisCluster(){
        return jedisCluster;
    }

    private void init(RedisProperties properties){
        if(properties.getCluster() != null && properties.getCluster().getNodes().size() > 0){
            logger.info("RedisClient work with cluster mode");
            mode = CLUSTER_MODE;
            RedisProperties.Cluster cluster = properties.getCluster();
            Set<HostAndPort> nodes = new HashSet<>();
            cluster.getNodes().forEach(val -> {
                String[] hostPortPair = val.split(":");
                nodes.add(new HostAndPort(hostPortPair[0].trim(), Integer.valueOf(hostPortPair[1])));
            });
            GenericObjectPoolConfig poolConfig = buildJedisPoolConfig(properties.getJedis().getPool());
            int connTimeout = properties.getConnTimeout();
            int soTimeout = properties.getReadTimeout();
            jedisCluster = new JedisCluster(nodes, connTimeout, soTimeout, cluster.getMaxRedirects(), cluster.getPassword(), poolConfig);
        }else if(properties.getSentinel() != null && properties.getSentinel().getNodes().size() > 0){
            logger.info("RedisClient work with sentinel mode");
            mode = SENTINEL_MODE;
            RedisProperties.Sentinel sentinel = properties.getSentinel();
            GenericObjectPoolConfig poolConfig = buildJedisPoolConfig(properties.getJedis().getPool());
            Set<String> nodes = new HashSet<>();
            sentinel.getNodes().forEach(val -> nodes.add(val));
            int connTimeout = properties.getConnTimeout();
            int soTimeout = properties.getReadTimeout();
            jedisPool = new JedisSentinelPool(sentinel.getMaster(), nodes, poolConfig, connTimeout, soTimeout,
                    sentinel.getPassword(), properties.getDatabase());
        }else if(Utils.isNotEmpty(properties.getHost())){
            logger.info("RedisClient work with standAlone mode");
            mode = STAND_ALONE_MODE;
            GenericObjectPoolConfig poolConfig = buildJedisPoolConfig(properties.getJedis().getPool());
            int connTimeout = properties.getConnTimeout();
            int soTimeout = properties.getReadTimeout();
            jedisPool = new JedisPool(poolConfig, properties.getHost(), properties.getPort(), connTimeout,
                    soTimeout, properties.getPassword(), 0, null, properties.getSsl());
        }else{
            throw new RuntimeException("请正确配置redis！");
        }
    }

    private GenericObjectPoolConfig buildJedisPoolConfig(RedisProperties.Pool pool){
        if(pool == null){
            return new GenericObjectPoolConfig();
        }

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if(pool.getMaxWait() != null){
            config.setMaxWaitMillis(pool.getMaxWait());
        }
        if(pool.getTimeBetweenEvictionRuns() != null){
            config.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns());
        }
        config.setTestOnBorrow(true);
        return config;
    }
}
