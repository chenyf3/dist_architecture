package com.xpay.sdk.api.utils;

public class StringUtil {
    public static boolean isEmpty(Object value){
        return value == null || value.toString().trim().length() == 0;
    }

    public static boolean isNotEmpty(Object value){
        return !isEmpty(value);
    }
}
