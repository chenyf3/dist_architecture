package com.xpay.common.statics.dto.migrate;

import java.io.Serializable;

public class MigrateParamDto implements Serializable {
    private static final long serialVersionUID = -16365463541231315L;
    private String migratorName;
    private String[] migrateItems;
    private int concurrent = 1;
    private int migrateSecond = 60 * 60;//任务迁移的大体时间，也是任务锁的持有时间，默认60分钟(支持自动续租的分布式锁不需要此参数)
    private String extraParam;

    public String getMigratorName() {
        return migratorName;
    }

    public void setMigratorName(String migratorName) {
        this.migratorName = migratorName;
    }

    public int getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(int concurrent) {
        this.concurrent = concurrent;
    }

    public String[] getMigrateItems() {
        return migrateItems;
    }

    public void setMigrateItems(String[] migrateItems) {
        this.migrateItems = migrateItems;
    }

    public int getMigrateSecond() {
        return migrateSecond;
    }

    public void setMigrateSecond(int migrateSecond) {
        this.migrateSecond = migrateSecond;
    }

    public String getExtraParam() {
        return extraParam;
    }

    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }
}
