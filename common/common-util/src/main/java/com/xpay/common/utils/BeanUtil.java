package com.xpay.common.utils;

import com.xpay.common.exception.UtilException;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtil {

    /**
     * 两个对象之间的属性复制，把source的值复制给dest
     * @param source
     * @param dest
     */
    public static <S,T> void copy(S source, T dest) {
        if(source == null || dest == null) {
            return;
        }

        try {
            BeanUtils.copyProperties(dest, source);
        } catch (Exception e) {
            throw new UtilException("对象属性复制异常", e);
        }
    }

    /**
     * 创建新对象并复制数据源对象的值到新对象上
     * @param source      数据源对象
     * @param destClz     目标对象的Class
     * @param <S>         数据源对象的类型
     * @param <T>         目标对象的类型
     * @return
     */
    public static <S,T> T newAndCopy(S source, Class<T> destClz) {
        if(source == null || destClz == null) {
            return null;
        }

        try {
            T target = destClz.newInstance();
            BeanUtils.copyProperties(target, source);
            return target;
        } catch (Exception e) {
            throw new UtilException("对象属性复制异常", e);
        }
    }

    /**
     * 创建新对象并复制数据源对象的值到新对象上
     * @param sourceList    数据源对象列表
     * @param destClz       目标对象的Class
     * @param <S>           数据源对象的类型
     * @param <T>           目标对象的类型
     * @return
     */
    public static <S,T> List<T> newAndCopy(List<S> sourceList, Class<T> destClz) {
        if(sourceList == null || sourceList.isEmpty() || destClz == null) {
            return new ArrayList<>();
        }

        List<T> targetList = new ArrayList<>(sourceList.size());
        for(S source : sourceList) {
            T target = newAndCopy(source, destClz);
            targetList.add(target);
        }
        return targetList;
    }

    /**
     * 将一个object对象转换成map,
     * 每一个field的名称为key,值为value
     *
     * @param object 当object为Null时,返回的结果一个size为0的map
     * @return map
     */
    public static <T> Map<String, Object> toMap(T object) {
        Map<String, Object> map = new HashMap<>();
        try {
            new BeanMap(object).forEach((k, v) -> {
                if (!k.equals("class")) {
                    map.put(k.toString(), v);
                }
            });
        } catch (Exception ex) {
            throw new UtilException("bean转换成map失败", ex);
        }
        return map;
    }

    public static <T> Map<String, Object> toMapNotNull(T object) {
        Map<String, Object> map = new HashMap<>();
        try {
            new BeanMap(object).forEach((k, v) -> {
                if (!k.equals("class") && v != null) {
                    map.put(k.toString(), v);
                }
            });
        } catch (Exception ex) {
            throw new UtilException("bean转换成map失败", ex);
        }
        return map;
    }
}
