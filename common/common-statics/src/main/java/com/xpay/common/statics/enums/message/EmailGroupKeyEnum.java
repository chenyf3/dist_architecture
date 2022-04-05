package com.xpay.common.statics.enums.message;

/**
 * 系统日常运营团队枚举类
 */
public enum EmailGroupKeyEnum {
    SYS_MONITOR_ALERT_GROUP(1, "系统监控预警小组"),
    ACCOUNT_ALERT_GROUP(2, "账务预警小组"),
    MIGRATION_ALERT_GROUP(3, "数据迁移预警小组"),
    DIST_LOCK_ALERT_GROUP(4, "分布式锁预警小组"),
    ;

    private int value;
    private String desc;

    private EmailGroupKeyEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static EmailGroupKeyEnum getEnum(String name){
        EmailGroupKeyEnum[] values = EmailGroupKeyEnum.values();
        for(int i=0; i<values.length; i++){
            if(values[i].name().equals(name)){
                return values[i];
            }
        }
        return null;
    }
}
