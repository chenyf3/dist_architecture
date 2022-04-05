package com.xpay.service.migration.migrators;

import com.xpay.common.statics.constants.migrate.MigratorName;
import com.xpay.common.statics.dto.migrate.DataMigrationDto;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.accountmch.service.AccountMigrationFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 平台商户账务数据迁移器
 */
@Component(MigratorName.ACCOUNT_MCH_MIGRATION)
public class AccountMchDataMigrator extends DataMigrator {
    @DubboReference(check = false)
    AccountMigrationFacade accountMigrationFacade;

    @Override
    public List<Date> listNeedMigrateDates(Date migrationEndDate, MigrateItemEnum migrateItem) {
        return accountMigrationFacade.listNeedMigrateDates(migrationEndDate, migrateItem);
    }

    @Override
    protected int doDataMigration(DataMigrationDto migrationDto) {
        return accountMigrationFacade.doDataMigration(migrationDto);
    }

    @Override
    protected int getDataKeepDays(MigrateItemEnum migrateItem) {
        switch (migrateItem) {
            case ACCOUNT_MCH_PROCESS_DETAIL:
                return 15;
            case ACCOUNT_MCH_PROCESS_PENDING:
            case ACCOUNT_MCH_PROCESS_RESULT:
                return 3;
            case ACCOUNT_MCH_COMMON_UNIQUE:
                return 60;
            default:
                throw new BizException("不支持的migrateItem: "+ migrateItem);
        }
    }
}
