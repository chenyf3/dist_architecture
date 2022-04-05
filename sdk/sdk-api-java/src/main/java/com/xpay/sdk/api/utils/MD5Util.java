package com.xpay.sdk.api.utils;

import com.xpay.sdk.api.exceptions.SDKException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类
 */
public class MD5Util {


    /**
     * 生成16进制的MD5字符串
     * @param str
     * @return
     */
    public static String getMD5Hex(String str) {
        return byte2Hex(getMD5(str));
    }

    public static byte[] getMD5(String str) {
        return getMD5(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String getMD5Hex(byte[] bytes) {
        return byte2Hex(getMD5(bytes));
    }

    public static byte[] getMD5(byte[] bytes) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成MD5信息时异常", e);
        }
        return messageDigest.digest();
    }

    public static String byte2Hex(byte[] bytes) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int j = bytes.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (byte byte0 : bytes) {
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }
}
