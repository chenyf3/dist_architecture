package com.xpay.service.migration.migrators;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.dto.migrate.MigrateParamDto;
import com.xpay.common.statics.enums.message.EmailGroupKeyEnum;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.wrapper.ThreadPoolWrapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/3/24
 */
@Component
public class DataMigrateHelper {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private BeanFactory beanFactory;
    private ThreadPoolWrapper shareThreadPool;
    private DistributedLock<RLock> distributedLock;

    @DubboReference
    EmailFacade emailFacade;

    public DataMigrateHelper(BeanFactory beanFactory,
                             ThreadPoolWrapper shareThreadPool,
                             DistributedLock<RLock> distributedLock){
        this.beanFactory = beanFactory;
        this.shareThreadPool = shareThreadPool;
        this.distributedLock = distributedLock;
    }

    /**
     * 提交数据迁移的任务
     * @param migrateParam
     */
    public final void submitMigrationTask(MigrateParamDto migrateParam) {
        logger.info("准备提交数据迁移任务 MigrateParamDto = {}", JsonUtil.toJson(migrateParam));

        paramValid(migrateParam);

        String resourceId = "dataMigration:" + migrateParam.getMigratorName();
        RLock lock = distributedLock.tryLock(resourceId, 5000, -1L);//加分布式锁，并设置锁为自动续租模式
        if (lock == null) {
            logger.info("获取锁失败，当前任务可能已经在执行,resourceId={}", resourceId);
            return;
        }

        addTask(migrateParam, () -> distributedLock.forceUnlock(lock));
    }

    private void addTask(MigrateParamDto migrateParam, Runnable callback) {
        //step1. 根据迁移器名称取得数据迁移器实例
        String migratorName = migrateParam.getMigratorName();
        DataMigrator migrator;
        try {
            migrator = beanFactory.getBean(migratorName, DataMigrator.class);
        } catch (Exception ex) {
            if(ex instanceof NoSuchBeanDefinitionException){
                logger.error("迁移器实例不存在 migratorName={}", migrateParam.getMigratorName());
            }else{
                logger.error("迁移器实例获取异常 migratorName={}", migrateParam.getMigratorName(), ex);
            }
            callback.run();
            return;
        }

        //step2. 把数据迁移明细从名称转换为枚举值
        List<MigrateItemEnum> itemList = new ArrayList<>();
        Arrays.asList(migrateParam.getMigrateItems()).forEach(itemName -> {
            MigrateItemEnum migrateItem = MigrateItemEnum.getEnum(itemName);
            if (migrateItem == null) {
                logger.warn("没有匹配到对应的MigrateItemEnum枚举值，将忽略, MigrateItemName = {}", itemName);
            } else {
                itemList.add(migrateItem);
            }
        });
        if (itemList.isEmpty()) {
            logger.info("没有任何匹配的MigrateItemEnum，本次任务不执行, migratorName={}", migratorName);
            callback.run();
            return;
        }

        //step3. 如果需要迁移的明细数量为1，则使用共享的线程池来执行数据迁移任务（因为不需要控制并发量）
        if(itemList.size() == 1){
            MigrateItemEnum migrateItem = itemList.get(0);
            shareThreadPool.execute(() -> {
                try {
                    migrator.startDataMigration(migrateItem);
                } catch (Throwable e) {
                    errorAlert(migratorName, migrateItem, e);
                } finally {
                    callback.run();
                }
            });
            return;
        }

        //step4. 如果需要迁移的明细数量大于1，则使用专有线程池来执行数据迁移任务，以便达到控制并发量的效果
        AtomicInteger taskCount = new AtomicInteger(itemList.size());
        int coreThread = itemList.size() > migrateParam.getConcurrent() ? migrateParam.getConcurrent() : itemList.size();
        ExecutorService executorService = Executors.newFixedThreadPool(coreThread);//控制并发量
        for(MigrateItemEnum migrateItem : itemList){
            executorService.submit(() -> {
                try {
                    migrator.startDataMigration(migrateItem);
                } catch (Throwable e) {
                    errorAlert(migratorName, migrateItem, e);
                } finally {
                    if (taskCount.decrementAndGet() <= 0) {
                        try{
                            callback.run();
                        }catch(Throwable e){
                        }
                        try{
                            executorService.shutdown();//使用完毕之后关闭线程池
                        }catch(Throwable e){
                        }
                    }
                }
            });
        }
    }

    private void paramValid(MigrateParamDto migrateParam) {
        if (migrateParam == null) {
            throw new BizException("migrateParam为空");
        } else if (StringUtil.isEmpty(migrateParam.getMigratorName())) {
            throw new BizException("migratorName为空");
        } else if (migrateParam.getMigrateItems() == null || migrateParam.getMigrateItems().length <= 0) {
            throw new BizException("migrateItems为空");
        } else if (migrateParam.getConcurrent() <= 0) {
            throw new BizException("concurrent需大于等于1");
        } else if (migrateParam.getMigrateSecond() <= 0) {
            throw new BizException("migrateSecond需大于等于1");
        }
    }

    private void errorAlert(String migratorName, MigrateItemEnum migrateItem, Throwable ex){
        logger.error("数据迁移过程中出现异常 migratorName={} migrateItem={} ", migratorName, migrateItem, ex);
        try{
            String subject = "数据迁移异常";
            StringBuilder content = new StringBuilder("执行数据迁移时发生异常")
                    .append(",migratorName=").append(migratorName)
                    .append(",migrateItem=").append(migrateItem.name())
                    .append(",Exception=").append(ex.getMessage());
            emailFacade.sendAsync(EmailGroupKeyEnum.MIGRATION_ALERT_GROUP.name(), subject, content.toString());
        }catch(Exception e){
            logger.error("发送数据迁移异常邮件时出现异常 migratorName={} migrateItem={}", migratorName, migrateItem, e);
        }
    }
}
