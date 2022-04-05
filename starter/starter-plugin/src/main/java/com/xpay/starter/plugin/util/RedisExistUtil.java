package com.xpay.starter.plugin.util;

public class RedisExistUtil {
    public static boolean isLettuceExist(){
        try {
            Class.forName("org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory");
            return true;
        } catch (Throwable e){
        }
        return false;
    }

    public static boolean isJedisExist(){
        try {
            Class.forName("org.springframework.data.redis.connection.jedis.JedisConnectionFactory");
            return true;
        } catch (Throwable e){
        }
        return false;
    }

    public static boolean isRedissonExist(){
        try {
            Class.forName("org.redisson.spring.data.connection.RedissonConnectionFactory");
            return true;
        } catch (Throwable e){
        }
        return false;
    }
}
