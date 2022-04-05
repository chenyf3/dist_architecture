package com.xpay.libs.id.common;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getLocalHost() {
        try{
            return InetAddress.getLocalHost().getHostName();
        }catch(Exception e){
            throw new RuntimeException("本地网络地址获取异常", e);
        }
    }

    public static String getLocalIp() {
        try{
            return InetAddress.getLocalHost().getHostAddress();
        }catch(Exception e){
            throw new RuntimeException("本地网络地址获取异常", e);
        }
    }

    public static boolean isEmpty(String str){
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    public static <T> T jsonToBean(String json, Class<T> clz){
        return JSON.parseObject(json, clz);
    }

    public static <T> String beanToJson(T obj){
        return JSON.toJSONString(obj);
    }
}
