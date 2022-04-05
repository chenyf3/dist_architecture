package com.xpay.service.accountmch.serviceImpl;

import com.xpay.facade.accountmch.service.AccountMigrationFacade;
import com.xpay.service.accountmch.biz.AccountMigrationBiz;
import com.xpay.common.statics.dto.migrate.DataMigrationDto;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @description: 平台商户账务数据迁移接口
 * @author: chenyf
 * @date: 2019-02-12
 */
@DubboService
public class AccountMigrationFacadeImpl implements AccountMigrationFacade {
    @Autowired
    AccountMigrationBiz accountMigrationBiz;

    /**
     * 计算需要迁移的日期
     * @param migrationEndDate
     * @param migrateItem
     * @return
     */
    public List<Date> listNeedMigrateDates(Date migrationEndDate, MigrateItemEnum migrateItem){
        return accountMigrationBiz.listNeedMigrateDates(migrationEndDate, migrateItem);
    }

    /**
     * 执行数据迁移
     * @param migrationDto
     * @return
     */
    public int doDataMigration(DataMigrationDto migrationDto){
        return accountMigrationBiz.doDataMigration(migrationDto);
    }
}
