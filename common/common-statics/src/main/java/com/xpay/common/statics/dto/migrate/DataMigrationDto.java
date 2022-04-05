package com.xpay.common.statics.dto.migrate;

import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import com.xpay.common.statics.exception.BizException;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: chenyf
 * @Date: 2018/1/2
 * @Description:
 */
public class DataMigrationDto implements Serializable {
    private static final long serialVersionUID = -1636546564645311315L;

    /**
     * 需要执行迁移的数据类型
     */
    private MigrateItemEnum migrateItem;

    /**
     * 当前被迁移数据的日期
     */
    private Date currMigrateDate;

    /**
     * 每次迁移多少条记录
     */
    private Integer migrateNumPerTime = 0;

    public MigrateItemEnum getMigrateItem() {
        return migrateItem;
    }

    public void setMigrateItem(MigrateItemEnum migrateItem) {
        this.migrateItem = migrateItem;
    }

    public Date getCurrMigrateDate() {
        return currMigrateDate;
    }

    public void setCurrMigrateDate(Date currMigrateDate) {
        this.currMigrateDate = currMigrateDate;
    }

    public Integer getMigrateNumPerTime() {
        return migrateNumPerTime;
    }

    public void setMigrateNumPerTime(Integer migrateNumPerTime) {
        this.migrateNumPerTime = migrateNumPerTime;
    }

    public static void validateMigrationParam(DataMigrationDto migrationDto){
        if(migrationDto == null){
            throw new BizException(BizException.PARAM_INVALID, "migrationDto不能为null");
        }else if(migrationDto.getCurrMigrateDate() == null){
            throw new BizException(BizException.PARAM_INVALID, "currMigrateDate不能为null");
        }else if(migrationDto.getMigrateNumPerTime() == null){
            throw new BizException(BizException.PARAM_INVALID, "migrateNumPerTime不能为null");
        }
    }
}
