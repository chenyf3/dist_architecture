package com.xpay.common.statics.dto.accountmch;

/**
 * Author: Cmf
 * Date: 2019.12.19
 * Time: 14:20
 * Description: 商户账户金额类型
 */
public enum AccountMchAmountTypeEnum {
    /**
     * 已结算金额
     */
    USABLE_AMOUNT(1, "可用余额"),

    /**
     * 待结算金额
     */
    UNSETTLE_AMOUNT(2, "待清算余额"),

    /**
     * 待清算转移到可用余额
     */
    UNSETTLE_CIRCULATE_USABLE_AMOUNT(3, "待清算到可用余额"),

    /**
     * 原出款金额（出款退回时使用）
     */
    SOURCE_DEBIT_AMOUNT(9, "原出款金额"),

    ;
    /**
     * 枚举值
     */
    private int value;
    /**
     * 描述
     */
    private String desc;

    AccountMchAmountTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }
}
