package com.xpay.common.statics.enums.migrate;

public enum MigrateItemEnum {
    ACCOUNT_MCH_PROCESS_DETAIL(1, "平台商户账务处理明细"),
    ACCOUNT_MCH_PROCESS_PENDING(2, "平台商户待账务处理"),
    ACCOUNT_MCH_PROCESS_RESULT(3, "平台商户账务处理结果"),
    ACCOUNT_MCH_COMMON_UNIQUE(4, "平台商户账务唯一约束"),


    ;

    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String desc;

    private MigrateItemEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static MigrateItemEnum getEnum(String name) {
        MigrateItemEnum[] values = MigrateItemEnum.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].name().equals(name)) {
                return values[i];
            }
        }
        return null;
    }
}
