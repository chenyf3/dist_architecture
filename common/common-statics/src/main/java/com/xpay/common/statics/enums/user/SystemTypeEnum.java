package com.xpay.common.statics.enums.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统类型
 *
 * @author longfenghua
 * @date 2019/11/15
 */
public enum SystemTypeEnum {
    COMMON_MANAGEMENT(1, "通用"),
    BOSS_MANAGEMENT(2, "运营后台"),
    MERCHANT_MANAGEMENT(3, "商户后台"),
    /**
     * 经营管家
     */
    JYGJ(4, "经营管家");

    /**
     * 枚举值
     */
    private int value;
    /**
     * 描述
     */
    private String desc;



    SystemTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static List<Map<String, Object>> toList() {
        return Arrays.stream(SystemTypeEnum.values()).map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("value", p.value);
            item.put("desc", p.desc);
            return item;
        }).collect(Collectors.toList());
    }
}
