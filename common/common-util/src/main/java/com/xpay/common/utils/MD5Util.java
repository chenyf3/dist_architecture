package com.xpay.common.utils;

import com.xpay.common.exception.UtilException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/5
 */
public class MD5Util {

    public static String getMD5Hex(String str) {
        return String.valueOf(Hex.encodeHex(getMD5(str), true));
    }

    /**
     * @param str
     * @return
     */
    public static byte[] getMD5(String str) {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        return getMD5(data);
    }

    public static String getMD5Hex(byte[] bytes) {
        return String.valueOf(Hex.encodeHex(getMD5(bytes), false));
    }

    public static byte[] getMD5(byte[] bytes) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new UtilException("生成MD5信息时异常", e);
        }
        return messageDigest.digest();
    }

}
