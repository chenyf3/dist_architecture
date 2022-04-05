package com.xpay.common.statics.enums.merchant;

import java.util.Arrays;

/**
 * 商户类型
 *
 * @author chenyf
 * @date 2020/04/26 16:19
 */
public enum MchTypeEnum {

    OIL_STATION(1, "加油站"),

    GROUP(2, "集团商户"),

    OP_CAR_TEAM(3, "采购车队"),

    PUR_CAR_TEAM(4, "运营车队"),

    ;

    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;

    MchTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static MchTypeEnum getEnum(int value) {
        return Arrays.stream(values()).filter(p -> p.value == value).findFirst().orElse(null);
    }
}
