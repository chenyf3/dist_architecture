package com.xpay.common.statics.enums.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @描述: 账户状态. 适用于账户表.
 * @作者: chenyf
 * @创建时间: 2019-09-01
 * @版本: 1.0
 */
public enum AccountStatusEnum {
    /**
     * 激活
     */
    ACTIVE(1, "激活"),
    /**
     * 冻结中
     */
    FREEZING(2, "冻结中"),
    /**
     * 冻结止收
     */
    FREEZE_CREDIT(3, "冻结止收"),
    /**
     * 冻结止付
     */
    FREEZE_DEBIT(4, "冻结止付"),
    /**
     * 注销
     */
    CANCELLED(5, "注销");

    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;

    private AccountStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static AccountStatusEnum getEnum(int value) {
        AccountStatusEnum resultEnum = null;
        AccountStatusEnum[] enumAry = AccountStatusEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue() == value) {
                resultEnum = enumAry[i];
                break;
            }
        }
        return resultEnum;
    }

    public static Map<String, Map<String, Object>> toMap() {
        AccountStatusEnum[] ary = AccountStatusEnum.values();
        Map<String, Map<String, Object>> enumMap = new HashMap<String, Map<String, Object>>();
        for (int num = 0; num < ary.length; num++) {
            Map<String, Object> map = new HashMap<String, Object>();
            String key = String.valueOf(getEnum(ary[num].getValue()));
            map.put("value", String.valueOf(ary[num].getValue()));
            map.put("desc", ary[num].getDesc());
            enumMap.put(key, map);
        }
        return enumMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List toList() {
        AccountStatusEnum[] ary = AccountStatusEnum.values();
        List list = new ArrayList();
        for (int i = 0; i < ary.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("value", String.valueOf(ary[i].getValue()));
            map.put("desc", ary[i].getDesc());
            list.add(map);
        }
        return list;
    }
}
