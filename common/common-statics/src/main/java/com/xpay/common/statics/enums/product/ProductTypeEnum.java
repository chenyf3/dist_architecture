package com.xpay.common.statics.enums.product;

import java.util.*;

/**
 * 业务类型，即业务线或产品线
 */
public enum ProductTypeEnum {
    PAY_RECEIVE(1, "收单"),
    REFUND(2, "退款"),
    LIQUIDATION(3, "清算"),
    SETTLE(4,"结算"),
    PAYMENT(5, "付款"),
    REMIT_RETURN(6, "退汇"),
    RCMS(7, "风控"),


    ;

    /** 枚举值 */
    private int value;

    /** 描述 */
    private String desc;

    private ProductTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static ProductTypeEnum getEnum(int value){
        return Arrays.stream(values()).filter(p -> p.value == value).findFirst().orElse(null);
    }

    public static List<Map<String, String>> toList() {
        ProductTypeEnum[] ary = ProductTypeEnum.values();
        List<Map<String, String>> list = new ArrayList();
        for (int i = 0; i < ary.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("value", String.valueOf(ary[i].getValue()));
            map.put("desc", ary[i].getDesc());
            list.add(map);
        }
        return list;
    }

    public static Map<String, String> toMap() {
        ProductTypeEnum[] ary = ProductTypeEnum.values();
        Map<String, String> enumMap = new HashMap<>();
        for (int num = 0; num < ary.length; num++) {
            enumMap.put(String.valueOf(ary[num].getValue()), ary[num].getDesc());
        }
        return enumMap;
    }
}
