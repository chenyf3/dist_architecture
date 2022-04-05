package com.xpay.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 编解码工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class CodeUtil {

    public static String base64Encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] base64Decode(String value) {
        return Base64.getDecoder().decode(value);
    }

    public static String urlEncode(String url){
        try {
            return URLEncoder.encode(url,  StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public static String urlDecode(String url){
        try {
            return URLDecoder.decode(url,  StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
}
