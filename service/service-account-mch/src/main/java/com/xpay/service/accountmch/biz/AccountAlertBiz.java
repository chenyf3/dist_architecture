package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.DistLockConst;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.statics.enums.message.EmailGroupKeyEnum;
import com.xpay.common.utils.DateUtil;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.starter.plugin.plugins.DistributedLock;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 账务预警逻辑层
 * 如果条件允许，此处理逻辑最好放在运维监控平台去查询从库，一是避免应用本身出故障时预警也一并失效了，二是这些查询可能会比较慢，查询从库可避免影响到主库的交易
 */
@Component
public class AccountAlertBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AccountProcessPendingBiz accountProcessPendingBiz;
    @Autowired
    AccountProcessResultBiz accountProcessResultBiz;
    @Autowired
    DistributedLock<RLock> distributedLock;

    @DubboReference
    EmailFacade emailFacade;

    /**
     * 待账务处理记录处于 '待处理' 状态的时间过长预警
     */
    public void accountProcessPendingPendingLongAlert(){
        String LockName = DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + "pendingRecordPendingTooLongAlert";
        RLock lock = distributedLock.tryLock(LockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, 30);

        try {
            List<Date> createDates = new ArrayList<>();
            createDates.add(new Date());
            createDates.add(DateUtil.addDay(new Date(), -1));

            Integer processStage = AccountProcessPendingStageEnum.PENDING.getValue();
            Integer timeDiffSecond = 10 * 60;

            int count = accountProcessPendingBiz.countProcessingTooLongRecord(createDates, processStage, timeDiffSecond);
            if(count <= 0){
                return;
            }

            String subject = "待账务处理记录在'待处理'状态停留时间过长";
            String content = "有"+count+"条待账务处理记录，在'待处理'状态停留时间超过"+timeDiffSecond+"秒";

            emailFacade.sendAsync(EmailGroupKeyEnum.ACCOUNT_ALERT_GROUP.name(), subject, content);
        } catch (Exception e) {
            logger.error("待账务处理记录'待处理'状态预警,出现异常", e);
        } finally {
            distributedLock.unlock(lock);
        }
    }

    /**
     * 待账务处理记录处于 '处理中' 状态的时间过长预警
     */
    public void accountProcessPendingProcessingLongAlert(){
        String LockName = DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + "pendingRecordProcessingTooLongAlert";
        RLock lock = distributedLock.tryLock(LockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, 30);

        try{
            List<Date> createDates = new ArrayList<>();
            createDates.add(new Date());
            createDates.add(DateUtil.addDay(new Date(), -1));

            Integer processStage = AccountProcessPendingStageEnum.PROCESSING.getValue();
            Integer timeDiffSecond = 1 * 60;

            int count = accountProcessPendingBiz.countProcessingTooLongRecord(createDates, processStage, timeDiffSecond);
            if(count <= 0){
                return;
            }

            String subject = "待账务处理记录在'处理中'状态停留时间过长";
            String content = "有"+count+"条待账务处理记录，在'处理中'状态停留时间超过"+timeDiffSecond+"秒";

            emailFacade.sendAsync(EmailGroupKeyEnum.ACCOUNT_ALERT_GROUP.name(), subject, content);
        } catch (Exception e) {
            logger.error("待账务处理记录'处理中'状态预警,出现异常", e);
        } finally {
            distributedLock.unlock(lock);
        }
    }

    /**
     * 账务处理结果需要审核的的预警
     */
    public void accountProcessResultAuditAlert(){
        String LockName = DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + "resultRecordAuditAlert";
        RLock lock = distributedLock.tryLock(LockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, 30);

        try{
            List<Date> createDates = new ArrayList<>();
            createDates.add(new Date());
            createDates.add(DateUtil.addDay(new Date(), -1));
            int count = accountProcessResultBiz.countNeedAuditRecord(createDates);

            String subject = "账务处理结果审核";
            String content = "有"+count+"条账务处理结果需要审核";

            emailFacade.sendAsync(EmailGroupKeyEnum.ACCOUNT_ALERT_GROUP.name(), subject, content);
        } catch (Exception e) {
            logger.error("账务处理结果审核预警,出现异常", e);
        } finally {
            distributedLock.unlock(lock);
        }
    }
}
