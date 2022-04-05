package com.xpay.sdk.api.utils;

import java.util.Random;
import java.util.UUID;

/**
 * 获取一些随机数的工具类
 */
public class RandomUtil {

    public static String get16LenStr(){
        if(getInt(2) == 0){
            return MD5Util.getMD5Hex(UUID.randomUUID().toString()).substring(16);
        }else{
            return MD5Util.getMD5Hex(UUID.randomUUID().toString()).substring(1, 17);
        }
    }

    public static String get32LenStr(){
        return MD5Util.getMD5Hex(UUID.randomUUID().toString());
    }

    public static int getInt(int max){
        Random random = new Random();
        return random.nextInt(max);
    }

    public static int getInt(int min, int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static void main(String[] args){
        System.out.println(get32LenStr());
    }
}
