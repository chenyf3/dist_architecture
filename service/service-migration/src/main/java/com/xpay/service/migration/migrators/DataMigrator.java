package com.xpay.service.migration.migrators;

import com.xpay.common.statics.dto.migrate.DataMigrationDto;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import com.xpay.common.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @Description: 数据迁移器
 * @author: chenyf
 * @Date: 2018/3/24
 */
public abstract class DataMigrator {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 默认记录的保留天数
     */
    private static final int DEFAULT_DATA_KEEP_DAYS = 15;
    /**
     * 默认每批次迁移的数量
     */
    private static final int DEFAULT_MIGRATION_NUM_EACH_TIME = 3000;

    /**
     * 启动数据迁移
     */
    public final void startDataMigration(MigrateItemEnum migrateItem) {
        logger.info("migrateItem = {} 迁移开始", migrateItem);
        if (migrateItem == null) {
            return;
        }

        this.processDataMigration(migrateItem);
        logger.info("migrateItem = {} 迁移结束", migrateItem);
    }

    /**
     * 处理数据迁移
     */
    private void processDataMigration(MigrateItemEnum migrateItem) {
        //step 1. 计算需要被执行迁移数据的截止日期
        Date migrationEndDate = this.calcMigrationEndDate(this.getDataKeepDays(migrateItem));
        logger.info("migrateItem={} migrationEndDate={}", migrateItem, DateUtil.formatDate(migrationEndDate));

        //step 2. 得出需要执行数据迁移的所有日期
        List<Date> needMigrateDates = this.listNeedMigrateDates(migrationEndDate, migrateItem);
        if (needMigrateDates == null || needMigrateDates.isEmpty()) {
            logger.info("migrateItem={} 没有需要进行数据迁移的记录", migrateItem);
            return;
        }

        //step 3. 遍历日期，然后逐天迁移数据
        for (int i=0; i<needMigrateDates.size(); i++) {
            Date currMigrateDate = needMigrateDates.get(i);
            DataMigrationDto migrationDto = new DataMigrationDto();
            migrationDto.setMigrateItem(migrateItem);
            migrationDto.setCurrMigrateDate(currMigrateDate);
            migrationDto.setMigrateNumPerTime(getMigrationNumEachTime(migrateItem));

            int migrateTimes = 0;//迁移执行的次数
            long totalMigratedCount = 0;
            while (true) {
                migrateTimes ++;
                int curMigratedCount = this.doDataMigration(migrationDto);
                logger.info("migrateItem={} currMigrateDate={} migrateTimes={} curMigratedCount={}", migrateItem, DateUtil.formatDate(migrationEndDate), migrateTimes, curMigratedCount);

                totalMigratedCount += curMigratedCount;
                if (curMigratedCount < migrationDto.getMigrateNumPerTime()) {
                    break;
                }
            }

            logger.info("migrateItem={} currMigrateDate={} 迁移完毕，共迁移{}条数据", migrateItem, DateUtil.formatDate(currMigrateDate), totalMigratedCount);
        }
    }

    /**
     * 计算需要执行数据迁移的日期
     * @param migrationEndDate
     * @param migrateItem
     * @return
     */
    public abstract List<Date> listNeedMigrateDates(Date migrationEndDate, MigrateItemEnum migrateItem);

    /**
     * 执行数据迁移
     *
     * @param migrationDto .
     * @return 返回迁移成功的数据条数
     */
    protected abstract int doDataMigration(DataMigrationDto migrationDto);

    /**
     * 取得数据需要保留的天数
     *
     * @return
     */
    protected int getDataKeepDays(MigrateItemEnum migrateItem) {
        return DEFAULT_DATA_KEEP_DAYS;
    }

    /**
     * 取得每次迁移数据的条数
     *
     * @return
     */
    protected int getMigrationNumEachTime(MigrateItemEnum migrateItem) {
        return DEFAULT_MIGRATION_NUM_EACH_TIME;
    }

    /**
     * 根据需要保留的天数计算迁移截止日期
     *
     * @param keepDays .
     * @return
     */
    private Date calcMigrationEndDate(int keepDays) {
        return DateUtil.convertDate(DateUtil.addDay(new Date(), (-1) * keepDays));
    }
}
