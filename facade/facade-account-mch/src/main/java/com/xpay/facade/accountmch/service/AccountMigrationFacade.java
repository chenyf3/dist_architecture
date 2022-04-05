package com.xpay.facade.accountmch.service;

import com.xpay.common.statics.dto.migrate.DataMigrationDto;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;

import java.util.Date;
import java.util.List;

/**
 * @description: 平台商户账务数据迁移接口
 * @author: chenyf
 * @date: 2019-02-12
 */
public interface AccountMigrationFacade {

    /**
     * 计算需要迁移的日期
     * @param migrationEndDate
     * @param migrateItem
     * @return
     */
    public List<Date> listNeedMigrateDates(Date migrationEndDate, MigrateItemEnum migrateItem);

    /**
     * 执行数据迁移
     * @param migrationDto
     * @return
     */
    public int doDataMigration(DataMigrationDto migrationDto);
}
