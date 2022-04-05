package com.xpay.common.statics.enums.account;

/**
 * @Description:账务处理类型
 * @author: chenyf
 * @Date: 2018/3/5
 */
public enum AccountProcessTypeEnum {
    /**
     * 入账
     */
    CREDIT(1, "入账"),

    /**
     * 出款扣款
     */
    DEBIT_OUT(2, "扣款"),

    /**
     * 出款退回
     */
    DEBIT_RETURN(3, "退回"),

    /**
     * 调增
     */
    ADJUST_ADD(4, "调增"),

    /**
     * 调增
     */
    ADJUST_SUB(5, "调减"),

    /**
     * 调整金额比例
     */
    ADJUST_AMOUNT_RATIO(6, "调整金额比例"),

    /**
     * 账户本身金额字段之间的流转
     */
    SELF_CIRCULATION(7, "自身流转"),

    ;

    /** 枚举值 */
    private int value;

    /** 描述 */
    private String desc;

    private AccountProcessTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static AccountProcessTypeEnum getEnum(int value){
        AccountProcessTypeEnum[] types = AccountProcessTypeEnum.values();
        for(int i=0; i<types.length; i++){
            if(value == types[i].getValue()){
                return types[i];
            }
        }
        return null;
    }
}
