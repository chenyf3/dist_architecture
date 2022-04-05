package com.xpay.common.statics.enums.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Cmf
 * Date: 2019/10/10
 * Time: 10:31
 * Description: 操作员状态
 */
public enum UserStatusEnum {
    ACTIVE(1, "激活"),
    INACTIVE(2, "冻结"),
    UNAUDITED(3, "未审核");

    /**
     * 枚举值
     */
    private int value;
    /**
     * 描述
     */
    private String desc;


    UserStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }


    public String getDesc() {
        return desc;
    }


    public static UserStatusEnum getEnum(int value) {
        return Arrays.stream(UserStatusEnum.values()).filter(p -> p.getValue() == value).findFirst().orElse(null);
    }

    public static List<Map<String, Object>> toList() {
        return Arrays.stream(UserStatusEnum.values()).map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("value", p.value);
            item.put("desc", p.desc);
            return item;
        }).collect(Collectors.toList());
    }

}
