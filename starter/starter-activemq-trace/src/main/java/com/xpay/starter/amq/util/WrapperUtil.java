package com.xpay.starter.amq.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class WrapperUtil {
    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };
    private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    private static Gson gson = new GsonBuilder().serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    public static String toJSONString(Object object){
        return gson.toJson(object);
    }

    public static String gen32RandomStr(){
        return getMD5Hex(UUID.randomUUID().toString());
    }

    public static String getLocalIp() {
        try{
            return InetAddress.getLocalHost().getHostAddress();
        }catch(Exception e){
            throw new RuntimeException("本地网络地址获取异常", e);
        }
    }

    public static String getMD5Hex(String str) {
        return encodeHex(getMD5(str), true);
    }

    public static byte[] getMD5(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            if (str != null) {
                messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成MD5信息时异常", e);
        }
        return messageDigest.digest();
    }

    public static String encodeHex(final byte[] data, boolean toLowerCase) {
        char[] chars = encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
        return String.valueOf(chars);
    }

    private static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
}
