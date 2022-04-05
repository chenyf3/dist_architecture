package com.xpay.common.utils;

import com.google.common.base.CaseFormat;

public class WordCaseUtil {
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

    public static boolean isUnderscore(String field){
        return field.indexOf("_") > 0;
    }

    public static void main(String[] args){
        String str = "hello_world";
        System.out.println(toCamelCase(str));
        System.out.println(toSnakeCase(str));

        str = "helloWorld";
        System.out.println(toCamelCase(str));
        System.out.println(toSnakeCase(str));

        str = "world";
        System.out.println(toCamelCase(str));
        System.out.println(toSnakeCase(str));

        str = "ID";
        System.out.println(toCamelCase(str));
        System.out.println(toSnakeCase(str));
    }
}
