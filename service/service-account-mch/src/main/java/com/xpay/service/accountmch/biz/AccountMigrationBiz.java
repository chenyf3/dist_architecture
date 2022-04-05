package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.dto.migrate.DataMigrationDto;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.service.accountmch.dao.AccountCommonUniqueDao;
import com.xpay.service.accountmch.dao.AccountProcessDetailDao;
import com.xpay.service.accountmch.dao.AccountProcessPendingDao;
import com.xpay.service.accountmch.dao.AccountProcessResultDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: chenyf
 * @Date: 2018/1/2
 * @Description:
 */
@Component
public class AccountMigrationBiz {
    private Logger logger = LoggerFactory.getLogger(AccountMigrationBiz.class);
    @Autowired
    AccountProcessDetailDao accountProcessDetailDao;
    @Autowired
    AccountProcessPendingDao accountProcessPendingDao;
    @Autowired
    AccountProcessResultDao accountProcessResultDao;
    @Autowired
    AccountCommonUniqueDao accountCommonUniqueDao;

    /**
     * 计算需要迁移的日期
     * @param migrationEndDate
     * @param migrateItem
     * @return
     */
    public List<Date> listNeedMigrateDates(Date migrationEndDate, MigrateItemEnum migrateItem){
        logger.info("migrationEndDate={} migrateItem={}", DateUtil.formatDate(migrationEndDate), migrateItem);
        if(migrationEndDate == null){
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "migrationEndDate不能为null");
        }else if(migrateItem == null){
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "migrateItem不能为null");
        }

        List<Date> dateList;
        if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_DETAIL.equals(migrateItem)){
            dateList = accountProcessDetailDao.listNeedMigrateDates(migrationEndDate);
        }else if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_PENDING.equals(migrateItem)){
            dateList = accountProcessPendingDao.listNeedMigrateDates(migrationEndDate);
        }else if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_RESULT.equals(migrateItem)){
            dateList = accountProcessResultDao.listNeedMigrateDates(migrationEndDate);
        }else if(MigrateItemEnum.ACCOUNT_MCH_COMMON_UNIQUE.equals(migrateItem)){
            dateList = accountCommonUniqueDao.listNeedMigrateDates(migrationEndDate);
        }else{
            dateList = new ArrayList<>();
        }
        return dateList;
    }

    /**
     * 执行数据迁移
     * @param migrationDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int doDataMigration(DataMigrationDto migrationDto){
        DataMigrationDto.validateMigrationParam(migrationDto);
        MigrateItemEnum migrateItem = migrationDto.getMigrateItem();

        List<Long> recordIdList;
        Date currMigrateDate = migrationDto.getCurrMigrateDate();
        Integer migrateNumPerTime = migrationDto.getMigrateNumPerTime();
        if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_DETAIL.equals(migrateItem)){
            recordIdList = accountProcessDetailDao.listIdsForMigration(currMigrateDate, migrateNumPerTime);
        }else if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_PENDING.equals(migrateItem)){
            recordIdList = accountProcessPendingDao.listIdsForMigration(currMigrateDate, migrateNumPerTime);
        }else if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_RESULT.equals(migrateItem)){
            recordIdList = accountProcessResultDao.listIdsForMigration(currMigrateDate, migrateNumPerTime);
        }else if(MigrateItemEnum.ACCOUNT_MCH_COMMON_UNIQUE.equals(migrateItem)){
            recordIdList = accountCommonUniqueDao.listIdsForMigration(currMigrateDate, migrateNumPerTime);
        }else{
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "未支持的migrateItem: "+ migrateItem.name());
        }

        logger.info("migrateItem={} migrationDate={} recordIdList.size={} 获取id完成", migrateItem, DateUtil.formatDate(migrationDto.getCurrMigrateDate()), recordIdList==null?0:recordIdList.size());
        if(recordIdList == null || recordIdList.size() <= 0){
            return 0;
        }

        int insertCount = 0, deleteCount = 0;
        if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_DETAIL.equals(migrateItem)){
            insertCount = accountProcessDetailDao.migrateToHistoryByIds(recordIdList);
            if(insertCount > 0){
                deleteCount = accountProcessDetailDao.deleteRecordByIdList(recordIdList);
            }
        }else if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_PENDING.equals(migrateItem)){
            insertCount = accountProcessPendingDao.migrateToHistoryByIds(recordIdList);
            if(insertCount > 0){
                deleteCount = accountProcessPendingDao.deleteRecordByIdList(recordIdList);
            }
        }else if(MigrateItemEnum.ACCOUNT_MCH_PROCESS_RESULT.equals(migrateItem)){
            insertCount = accountProcessResultDao.migrateToHistoryByIds(recordIdList);
            if(insertCount > 0){
                deleteCount = accountProcessResultDao.deleteRecordByIdList(recordIdList);
            }
        }else if(MigrateItemEnum.ACCOUNT_MCH_COMMON_UNIQUE.equals(migrateItem)){
            deleteCount = accountCommonUniqueDao.deleteRecordByIdList(recordIdList);
            insertCount = deleteCount;
        }

        //如果两边不一致，则抛出异常让当前事务回滚
        if(insertCount > 0 && insertCount != deleteCount){
            logger.error("insertCount={} deleteCount={} DataMigrationDto={}", insertCount, deleteCount, JsonUtil.toJsonPretty(migrationDto));
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "数据迁移时插入记录数与删除记录数不一致");
        }
        return insertCount;
    }
}
