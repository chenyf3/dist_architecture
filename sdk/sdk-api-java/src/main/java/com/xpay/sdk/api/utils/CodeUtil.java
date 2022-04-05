package com.xpay.sdk.api.utils;

import java.util.Base64;

/**
 * 编解码工具类
 */
public class CodeUtil {

    public static String base64Encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] base64Decode(String value) {
        return Base64.getDecoder().decode(value);
    }
}
