package com.xpay.common.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jo on 2017/8/12.
 */
public class StringUtil {
    public final static String NORMAL_LETTER_REGEX = "^[a-zA-Z][a-zA-Z0-9_-]*$";//只允许英文字母开头，第2位以后允许字母、数字、下划线、中划线

    /**
     * 判断是否只包含常规字符，即：只允许英文字母开头，第2位以后允许字母、数字、下划线、中划线
     * @param content
     * @return
     */
    public static boolean isNormalLetter(String content){
        Pattern pattern = Pattern.compile(NORMAL_LETTER_REGEX);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.toString().trim().length() == 0;
    }

    public static boolean isNotEmpty(CharSequence value) {
        return !isEmpty(value);
    }

    public static boolean isLengthOver(String str, int maxLen) {
        if (str == null) {
            return false;
        } else {
            return str.length() > maxLen;
        }
    }

    public static boolean isLengthOk(String str, int minLen, int maxLen) {
        if (str == null) {
            return false;
        } else {
            return minLen <= str.length() && str.length() <= maxLen;
        }
    }

    public static String getUUIDStr() {
        return UUID.randomUUID().toString();
    }

    public static String getMD5UUIDStr() {
        return MD5Util.getMD5Hex(UUID.randomUUID().toString());
    }


    /**
     * 取字符串前n位，如果原字符串不足n位,则返回原字符串
     *
     * @param str .
     * @param n   .
     * @return
     */
    public static String subLeft(String str, int n) {
        if (str == null || str.length() <= n) {
            return str;
        } else {
            return str.substring(0, n);
        }
    }

    /**
     * 取字符串后n位，如果原字符串不足n位,则返回原字符串
     *
     * @param str .
     * @param n   .
     * @return
     */
    public static String subRight(String str, int n) {
        if (str == null || str.length() <= n) {
            return str;
        } else {
            return str.substring(str.length() - n);
        }
    }

    /**
     * 把一个数组用指定的分隔符分割组成字符串
     * @param delimiter
     * @param arr
     * @return
     */
    public static String join(String delimiter, String... arr){
        return String.join(delimiter, Arrays.asList(arr));
    }

    public static void main(String[] args){
        System.out.println(String.format("%1$03d", 5245));
    }
}
