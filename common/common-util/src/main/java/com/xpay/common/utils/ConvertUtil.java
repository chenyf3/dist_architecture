package com.xpay.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Cmf
 * Date: 2019/10/22
 * Time: 10:59
 * Description: 基本数据转换类
 */
public class ConvertUtil {

    /**
     * 转换成Integer,
     * 若转换失败，则返回null
     *
     * @param str .
     * @return
     */
    public static Integer stringToInteger(String str) {
        try {
            return StringUtil.isEmpty(str) ? null : Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 若转换失败，则返回Null
     *
     * @param str .
     * @return
     */
    public static Long stringToLong(String str) {
        try {
            return StringUtil.isEmpty(str) ? null : Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 若转换失败，则返回null.
     *
     * @param str .
     * @return
     */
    public static Double stringToDouble(String str) {
        try {
            return StringUtil.isEmpty(str) ? null : Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 指定每组的数量进行分组
     * @param dataList      需要分组的数据
     * @param numPerGroup   每组数量
     * @return
     */
    public static <T> Map<Integer, List<T>> splitGroup(List<T> dataList, int numPerGroup){
        //当前组号
        int currentGroupNo = 1;
        //当前组的数量
        int numOfCurrentGroup = 0;

        int size = dataList.size();
        Map<Integer, List<T>> groupMap = new HashMap();
        for(int i = 0;i < size; i++){
            List<T> batchList;
            if((batchList = groupMap.get(currentGroupNo)) == null){
                batchList = new ArrayList<>();
                groupMap.put(currentGroupNo, batchList);
            }
            batchList.add(dataList.get(i));

            numOfCurrentGroup++;
            if(numOfCurrentGroup < numPerGroup){
                continue;
            }else{
                numOfCurrentGroup = 0;
                currentGroupNo ++;
            }
        }
        return groupMap;
    }
}
