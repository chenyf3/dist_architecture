package com.xpay.starter.plugin.client;

import com.xpay.starter.plugin.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.types.Expiration;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis客户端
 * @author chenyf
 */
public class RedisClient {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private FactoryType factoryType;//RedisConnectionFactory 的类型
    private final Charset charset = StandardCharsets.UTF_8;
    private String name;//当前客户端名称
    private RedisConnectionFactory connectionFactory;

    private enum FactoryType {
        JEDIS,
        LETTUCE,
        REDISSON,
        OTHER//出现这种情况有可能是使用了自定义的实现，或者开启了spring的代理等
    }

    public RedisClient(String name, RedisConnectionFactory connectionFactory) {
        if(connectionFactory == null){
            throw new IllegalArgumentException("RedisConnectionFactory can not be NULL");
        }
        this.name = name;
        this.connectionFactory = connectionFactory;
        this.decideFactoryType();
        logger.info("RedisClient Build Success: {clientName:{}, factoryType:{}, clientHash:{}}", this.name, factoryType, this.hashCode());
    }

    public String get(String key){
        RedisConnection conn = getConnection();
        try {
            byte[] data = conn.get(serialize(key));
            return deserialize(data);
        } finally {
            releaseConnection(conn);
        }
    }

    public <T> T get(String key, Class<T> clz){
        RedisConnection conn = getConnection();
        try {
            byte[] data = conn.get(serialize(key));
            return deserialize(data, clz);
        } finally {
            releaseConnection(conn);
        }
    }

    public Boolean set(String key, String value){
        RedisConnection conn = getConnection();
        try {
            return conn.set(serialize(key), serialize(value));
        } finally {
            releaseConnection(conn);
        }
    }

    public <T> Boolean set(String key, T value, int seconds){
        if (seconds == 0) {
            throw new IllegalArgumentException("the expire second cannot be zero");
        }

        Expiration expiration = seconds <= -1 ? Expiration.persistent() : Expiration.from(seconds, TimeUnit.SECONDS);
        RedisConnection conn = getConnection();
        try {
            return conn.set(serialize(key), serialize(value), expiration, RedisStringCommands.SetOption.UPSERT);
        } finally {
            releaseConnection(conn);
        }
    }

    public String hget(String key, String field) {
        RedisConnection conn = getConnection();
        try {
            byte[] data = conn.hGet(serialize(key), serialize(field));
            return deserialize(data);
        } finally {
            releaseConnection(conn);
        }
    }

    public Boolean hset(String key, String field, String value) {
        RedisConnection conn = getConnection();
        try {
            return conn.hSet(serialize(key), serialize(field), serialize(value));
        } finally {
            releaseConnection(conn);
        }
    }

    public Set<String> hkeys(String key) {
        RedisConnection conn = getConnection();
        try {
            Set<byte[]> keys = conn.hKeys(serialize(key));
            Set<String> keySet = new HashSet<>();
            if(keys != null){
                keys.forEach(keyBytes -> keySet.add(deserialize(keyBytes)));
            }
            return keySet;
        } finally {
            releaseConnection(conn);
        }
    }

    public boolean del(String key) {
        RedisConnection conn = getConnection();
        try {
            Long count = conn.del(serialize(key));
            return count != null && count == 1;
        } finally {
            releaseConnection(conn);
        }
    }

    public boolean hdel(String key, String field){
        RedisConnection conn = getConnection();
        try {
            Long count = conn.hDel(serialize(key), serialize(field));
            return count != null && count == 1;
        } finally {
            releaseConnection(conn);
        }
    }

    public Long hincr(String key, String field){
        RedisConnection conn = getConnection();
        try {
            return conn.hIncrBy(serialize(key), serialize(field), 1);
        } finally {
            releaseConnection(conn);
        }
    }

    public Long incr(String key){
        RedisConnection conn = getConnection();
        try {
            return conn.incr(serialize(key));
        } finally {
            releaseConnection(conn);
        }
    }

    public Long incrBy(String key, int increment){
        RedisConnection conn = getConnection();
        try {
            return conn.incrBy(serialize(key), increment);
        } finally {
            releaseConnection(conn);
        }
    }

    public  <T> T eval(String script, List<String> keys, List<String> args, Class<T> clz){
        byte[][] keysAndArgs = getKeysAndArgsByteArray(keys, args);
        RedisConnection conn = getConnection();
        try {
            return conn.eval(serialize(script), ReturnType.fromJavaType(clz), keys.size(), keysAndArgs);
        } finally {
            releaseConnection(conn);
        }
    }

    public <T> T evalsha(String scriptSha, List<String> keys, List<String> args, Class<T> clz){
        byte[][] keysAndArgs = getKeysAndArgsByteArray(keys, args);
        RedisConnection conn = getConnection();
        try {
            return conn.evalSha(scriptSha, ReturnType.fromJavaType(clz), keys.size(), keysAndArgs);
        } finally {
            releaseConnection(conn);
        }
    }

    public Boolean exists(String key){
        RedisConnection conn = getConnection();
        try {
            return conn.exists(serialize(key));
        } finally {
            releaseConnection(conn);
        }
    }

    public long loopIncrId(String key, int incrStep, long maxValue){
        if(incrStep > maxValue){
            throw new RuntimeException("incrStep="+incrStep+",cannot bigger than maxValue="+maxValue);
        }else if(maxValue == Long.MAX_VALUE){
            maxValue = Long.MAX_VALUE - 1;//不能超过Long型最大值，因为Long的最后一个值预留给了resetLoopNum(...)方法中的Lua脚本自己用
        }
        Long id = incrBy(key, incrStep);
        if(id > maxValue){
            id = resetLoopNum(key, incrStep, maxValue);
        }
        return id;
    }

    /**
     * @param script
     * @return
     */
    public String scriptLoad(String script){
        RedisConnection conn = getConnection();
        try {
            return conn.scriptLoad(serialize(script));
        } finally {
            releaseConnection(conn);
        }
    }

    public Long resetLoopNum(String key, int incrStep, long maxValue){
        String script = "" +
                "local maxValue = tonumber(ARGV[2]);" +
                "local currValue = tonumber(redis.call('incrby', KEYS[1], ARGV[1]));" +
                "if currValue > maxValue then " +
                "  redis.call('set', KEYS[1], 0);" +
                "  currValue = tonumber(redis.call('incrby', KEYS[1], ARGV[1]));" +
                "end;" +
                "return currValue;";
        List<String> args = Arrays.asList(incrStep+"", maxValue+"");
        Long id = eval(script, Collections.singletonList(key), args, Long.class);
        return id;
    }

    public void destroy(){
        RedisConnectionFactory connectionFactory = this.connectionFactory;
        if(connectionFactory == null){
            return;
        }

        this.connectionFactory = null;
        try {
            if(factoryType != null && factoryType == FactoryType.JEDIS){
                ((org.springframework.data.redis.connection.jedis.JedisConnectionFactory) connectionFactory).destroy();
            }else if(factoryType != null && factoryType == FactoryType.LETTUCE){
                ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) connectionFactory).destroy();
            }else if(factoryType != null && factoryType == FactoryType.REDISSON){
                ((org.redisson.spring.data.connection.RedissonConnectionFactory) connectionFactory).destroy();
            }else{
                Method method = this.connectionFactory.getClass().getMethod("destroy");//如果不存在此方法，会抛出异常
                method.invoke(this.connectionFactory);
            }
            logger.info("connectionFactory destroy finish!! clientName:{}, factoryType:{}", name, factoryType);
        } catch (Throwable e) {
        }
    }

    public String getName(){
        return this.name;
    }

    private byte[][] getKeysAndArgsByteArray(List<String> keys, List<String> args){
        byte[][] keysAndArgs = new byte[keys.size() + args.size()][];
        int i = 0;
        for(String key : keys){
            keysAndArgs[i] = serialize(key);
            i ++;
        }
        for(String arg : args){
            keysAndArgs[i] = serialize(arg);
            i ++;
        }
        return keysAndArgs;
    }
    private RedisConnection getConnection(){
        return RedisConnectionUtils.getConnection(connectionFactory);
    }
    private void releaseConnection(RedisConnection connection){
        RedisConnectionUtils.releaseConnection(connection, connectionFactory, false);
    }
    private byte[] serialize(String string) {
        return (string == null ? null : string.getBytes(charset));
    }
    private <T> byte[] serialize(T obj) {
        String valueStr = obj == null ? null : obj instanceof String ? (String)obj : Utils.toJson(obj);
        return serialize(valueStr);
    }
    private String deserialize(byte[] bytes) {
        return (bytes == null ? null : new String(bytes, charset));
    }
    private <T> T deserialize(byte[] bytes, Class<T> clz) {
        return Utils.jsonToBean(bytes, clz);
    }
    private void decideFactoryType(){
        try {
            if(connectionFactory instanceof org.springframework.data.redis.connection.jedis.JedisConnectionFactory){
                this.factoryType = FactoryType.JEDIS;
                return;
            }
        } catch (Throwable e){
        }

        try {
            if(connectionFactory instanceof org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory){
                this.factoryType = FactoryType.LETTUCE;
                return;
            }
        } catch (Throwable e){
        }

        try {
            if(connectionFactory instanceof org.redisson.spring.data.connection.RedissonConnectionFactory){
                this.factoryType = FactoryType.REDISSON;
                return;
            }
        } catch (Throwable e){
        }

        this.factoryType = FactoryType.OTHER;
    }
}
