package com.xpay.common.statics.enums.product;

import java.util.*;

/**
 * 业务编码，即产品编码
 */
public enum ProductCodeEnum {
    RECEIVE_NET_B2C(1, "网银B2C收单", 1),
    RECEIVE_WECHAT_H5(2, "微信支付收单", 1),

    PAYMENT_ADVANCE(3, "垫资代付", 5),

    SETTLE_ADVANCE_CLEAR(4, "垫资账户清零", 3),
    SETTLE_SETTLE_TO_ACCOUNT(5, "结算到账", 4),
    SETTLE_REMIT(6, "结算打款", 4),

    ADVANCE_ADJUST(7, "垫资调整", 7),
    ;

    /** 枚举值 */
    private int value;

    /** 描述 */
    private String desc;

    /** 业务线编号，请参照 {@link ProductTypeEnum} */
    private int type;

    private ProductCodeEnum(int value, String desc, int type) {
        this.value = value;
        this.desc = desc;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static ProductCodeEnum getEnum(int value) {
        return Arrays.stream(values()).filter(p -> p.value == value).findFirst().orElse(null);
    }

    public static List<ProductCodeEnum> getEnumByType(int type) {
        ProductCodeEnum[] allEnums = ProductCodeEnum.values();
        List<ProductCodeEnum> matchEnums = new ArrayList<>();
        for(ProductCodeEnum codeEnum : allEnums){
            if(codeEnum.getType() == type){
                matchEnums.add(codeEnum);
            }
        }
        return matchEnums;
    }

    public static List<Map<String, String>> getMapByType(int type) {
        ProductCodeEnum[] allEnums = ProductCodeEnum.values();

        List<Map<String, String>> list = new ArrayList<>();
        for(ProductCodeEnum codeEnum : allEnums){
            if(codeEnum.getType() == type){
                Map<String, String> map = new HashMap<>();
                map.put(String.valueOf(codeEnum.getValue()), codeEnum.getDesc());
                list.add(map);
            }
        }
        return list;
    }

    public static List<Map<String, String>> toList(){
        List<Map<String, String>> list = new ArrayList<>();
        ProductCodeEnum[] values = ProductCodeEnum.values();
        for(int i=0; i<values.length; i++){
            Map<String, String> map = new HashMap<>();
            map.put("value", String.valueOf(values[i].getValue()));
            map.put("desc", values[i].getDesc());
            map.put("type", String.valueOf(values[i].getType()));
            list.add(map);
        }
        return list;
    }

    public static Map<String, Map<String, String>> toTypeMap() {
        ProductCodeEnum[] ary = ProductCodeEnum.values();
        Map<String, Map<String, String>> typeMap = new HashMap<>();
        for (int i = 0; i < ary.length; i++) {
            String type = String.valueOf(ary[i].getType());
            Map<String, String> map = typeMap.get(type);
            if(map == null){
                map = new HashMap<>();
                typeMap.put(type, map);
            }
            map.put(String.valueOf(ary[i].getValue()), ary[i].getDesc());
        }
        return typeMap;
    }
}
