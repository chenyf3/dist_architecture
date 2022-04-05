package com.xpay.sdk.api.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;
import java.util.List;

/**
 * JSON转换工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class JsonUtil {
    static SerializeConfig serializeSnakeCase = new SerializeConfig();
    static ParserConfig parserCamelCase = new ParserConfig();

    static {
        serializeSnakeCase.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        parserCamelCase.propertyNamingStrategy = PropertyNamingStrategy.CamelCase;
    }


    /**
     * 将一个对像转成一个json字符串
     */
    public static final String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 把字段的属性命名方式从驼峰转成下划线(只对POJO起效，对Map的key不起效)
     * @param obj
     * @return
     */
    public static String toJsonUnderline(Object obj){
        return JSON.toJSONString(obj, serializeSnakeCase, SerializerFeature.SortField, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 把json字符串转换成指定Class的对象
     * @param text
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * 把json字节转换成指定Class的对象
     * @param text
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toBean(byte[] text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * 把json字节转换成指定Class的对象
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toBeanOrderly(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz, Feature.OrderedField);
    }

    public static <T> T toBeanOrderly(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz, Feature.OrderedField);
    }

    /**
     * 把json字符串转换成指定类型的对象
     * @param text
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T toBean(String text, Type type) {
        return JSON.parseObject(text, type);
    }

    /**
     * 把json字符串转换为指定Class的List
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public static final <T> List<T> toList(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        return JSONArray.parseArray(obj.toString(), clazz);
    }
}
