package com.xpay.common.statics.enums.user;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户类型
 */
public enum UserTypeEnum {

    /**
     * 管理员
     **/
    ADMIN(1, "管理员"),

    /**
     * 普通用户
     **/
    USER(2, "普通用户");


    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;


    UserTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }


    public static UserTypeEnum getEnum(int value) {
        return Arrays.stream(values()).filter(p -> Objects.equals(value, p.getValue())).findFirst().orElse(null);
    }

    public static List<Map<String, Object>> toList() {
        return Arrays.stream(values()).map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("value", p.value);
            item.put("desc", p.desc);
            return item;
        }).collect(Collectors.toList());
    }

    public static Map<String, Map<String, Object>> toMap() {
        return Arrays.stream(UserTypeEnum.values())
                .collect(Collectors.toMap(Enum::name, p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("value", p.value);
                    item.put("desc", p.desc);
                    return item;
                }));
    }
}
