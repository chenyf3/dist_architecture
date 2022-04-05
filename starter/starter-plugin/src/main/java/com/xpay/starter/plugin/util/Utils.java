package com.xpay.starter.plugin.util;

import com.google.common.base.CaseFormat;
import com.google.gson.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 当前模块的公用工具类
 */
public class Utils {
    private static Gson gson;
    private static ClassLoader classLoader;

    static {
//        gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                .create();

        classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 判断是否为空
     * @param value
     * @return
     */
    public static boolean isEmpty(CharSequence value) {
        return value == null || value.toString().trim().length() == 0;
    }

    /**
     * 判断是否非空
     * @param value
     * @return
     */
    public static boolean isNotEmpty(CharSequence value) {
        return !isEmpty(value);
    }

    /**
     * 把对象转换成JSON字符串
     * @param object
     * @return
     */
    public static String toJson(Object object){
        return gson.toJson(object);
    }

    /**
     * 把json字符串转换成指定Class的对象
     * @param text
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonToBean(String text, Class<T> clazz) {
        if(text == null || clazz == null) {
            return null;
        }
        return gson.fromJson(text, clazz);
    }

    /**
     * 把json字节转换成指定Class的对象
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonToBean(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null) {
            return null;
        }
        String str = new String(bytes, StandardCharsets.UTF_8);
        return jsonToBean(str, clazz);
    }

    /**
     * 根据class的类名称取得这个类的java.lang.Class对象
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class getClass(String className) throws ClassNotFoundException {
        return Class.forName(className, true, classLoader);
    }

    /**
     * 把下划线转成驼峰
     * @param fieldName
     * @return
     */
    public static String toCamelCase(String fieldName){
        if(isUnderscore(fieldName)){//输入参数是有下划线的则转为驼峰
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName);
        }else if(isCamel(fieldName)){//输入参数已经是驼峰的则直接返回
            return fieldName;
        }else{//输入参数是单个字母的转为小写
            return fieldName.toLowerCase();
        }
    }

    /**
     * 把驼峰转成下划线(大写)
     * @param fieldName
     * @return
     */
    public static String toSnakeCase(String fieldName){
        if(isCamel(fieldName)){//输入参数是驼峰的则转为下划线
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
        }else if(isUnderscore(fieldName)){//输入参数已经是下划线的则直接返回
            return fieldName;
        }else{//输入参数是单个字母的转为大写
            return fieldName.toUpperCase();
        }
    }

    /**
     * 判断变量是否驼峰规则命名
     * @param field
     * @return
     */
    public static boolean isCamel(String field){
        int upperCount = 0, lowerCount = 0;
        char[] arr = field.toCharArray();
        for(int i=0; i<arr.length; i++){
            if('a' <= arr[i] && arr[i] <= 'z'){
                lowerCount ++;
            }else if('A' <= arr[i] && arr[i] <= 'Z'){
                upperCount ++;
            }
            if(upperCount > 0 && lowerCount > 0){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断变量是否下划线规则命名
     * @param field
     * @return
     */
    public static boolean isUnderscore(String field){
        return field.indexOf("_") > 0;
    }

    /**
     * 转换为东八区时区(Asia/Shanghai)
     * @param zonedDateTime
     * @return
     */
    public static Date shanghaiTimeZone(ZonedDateTime zonedDateTime){
        if (zonedDateTime == null) {
            return null;
        }
        ZonedDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        return Date.from(dateTime.toInstant());
    }


    /**
     * 是否以指定字符串开头，忽略大小写
     *
     * @param str    被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
    }

    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param str        被监测字符串
     * @param prefix     开头字符串
     * @param ignoreCase 是否忽略大小写
     * @return 是否以指定字符串开头
     * @since 5.4.3
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
        return startWith(str, prefix, ignoreCase, false);
    }

    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param str          被监测字符串
     * @param prefix       开头字符串
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @return 是否以指定字符串开头
     * @since 5.4.3
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == str || null == prefix) {
            if (false == ignoreEquals) {
                return false;
            }
            return null == str && null == prefix;
        }

        boolean isStartWith;
        if (ignoreCase) {
            isStartWith = str.toString().toLowerCase().startsWith(prefix.toString().toLowerCase());
        } else {
            isStartWith = str.toString().startsWith(prefix.toString());
        }

        if (isStartWith) {
            return (false == ignoreEquals) || (false == equals(str, prefix, ignoreCase));
        }
        return false;
    }

    /**
     * 比较两个字符串是否相等。
     *
     * @param str1       要比较的字符串1
     * @param str2       要比较的字符串2
     * @param ignoreCase 是否忽略大小写
     * @return 如果两个字符串相同，或者都是{@code null}，则返回{@code true}
     * @since 3.2.0
     */
    public static boolean equals(CharSequence str1, CharSequence str2, boolean ignoreCase) {
        if (null == str1) {
            // 只有两个都为null才判断相等
            return str2 == null;
        }
        if (null == str2) {
            // 字符串2空，字符串1非空，直接false
            return false;
        }

        if (ignoreCase) {
            return str1.toString().equalsIgnoreCase(str2.toString());
        } else {
            return str1.toString().contentEquals(str2);
        }
    }

    /**
     * 把字符串以指定字符集编码成字节数组
     * @param str     字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str, Charset charset) {
        if (str == null) {
            return null;
        }

        if (null == charset) {
            return str.toString().getBytes();
        }
        return str.toString().getBytes(charset);
    }

    /**
     * 读取字节数组的前28个字节并转换成大写的16进制
     * @param source
     * @return
     */
    public static String read28UpperHex(byte[] source) {
        return HexUtil.encodeHexStr(readBytes(source, 28), false);
    }

    /**
     * 把InputStream转换成字节数组
     * @param input
     * @return
     */
    public static byte[] readBytes(InputStream input) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int readBytes;
            byte[] buffer = new byte[1024];
            while ((readBytes = input.read(buffer)) != -1) {
                output.write(buffer, 0, readBytes);
            }
            return output.toByteArray();
        } catch (IOException e){
            throw new RuntimeException(e);
        } finally {
            try {
                input.close();
            } catch (Exception e){
            }
        }
    }

    /**
     * 从InputStream中读取指定的字节长度
     * @param input     数据源
     * @param length    需要读取的字节数
     * @return
     */
    public static byte[] readBytes(InputStream input, int length) {
        if (null == input) {
            return null;
        } else if (length <= 0) {
            return new byte[0];
        } else {
            byte[] buf = new byte[length];

            int readLength;
            try {
                readLength = input.read(buf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (readLength > 0 && readLength < length) {
                //说明InputStream中的总字节数小于length指定的长度
                byte[] buf2 = new byte[readLength];
                System.arraycopy(buf, 0, buf2, 0, readLength);
                return buf2;
            } else {
                return buf;
            }
        }
    }

    /**
     * 从一个字节数组中读取指定的字节长度
     * @param source
     * @param length
     * @return
     */
    public static byte[] readBytes(byte[] source, int length) {
        if(source == null || source.length == 0){
            return new byte[0];
        }

        if(source.length < length){
            length = source.length;
        }
        byte[] buffer = new byte[length];
        System.arraycopy(source, 0, buffer, 0, length);//拷贝数组
        return buffer;
    }
}
