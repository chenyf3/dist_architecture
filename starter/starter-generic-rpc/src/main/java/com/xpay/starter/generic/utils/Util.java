package com.xpay.starter.generic.utils;

public class Util {

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.toString().trim().length() == 0;
    }

    public static boolean isNotEmpty(CharSequence value) {
        return !isEmpty(value);
    }
}
