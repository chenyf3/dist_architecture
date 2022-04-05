package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.DistLockConst;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.statics.enums.account.AccountProcessResultCallbackStageEnum;
import com.xpay.common.utils.ConvertUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.accountmch.dto.PendingInfoDto;
import com.xpay.starter.plugin.plugins.DistributedLock;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时账务处理
 */
@Component
public class AccountScheduleBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AccountProcessPendingBiz accountProcessPendingBiz;
    @Autowired
    AccountProcessHandler accountProcessHandler;
    @Autowired
    AccountProcessResultBiz accountProcessResultBiz;
    @Autowired
    DistributedLock<RLock> distributedLock;

    public void scanPendingAndDoAccountProcess() {
        String LockName = DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + "scheduleAccountMchProcess";
        RLock lock = distributedLock.tryLock(LockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, -1);
        try {
            //为了能使用到索引，加上createDate这个条件，为了避免在跨日时漏查数据，所以需要查询近两天内的数据
            long start = System.currentTimeMillis();
            List<Date> createDates = new ArrayList<>();
            createDates.add(new Date());
            createDates.add(DateUtil.addDay(new Date(), -1));

            int offset = 0, numberCurrPage = 0, numberPerPage = 200, totalNum = 0;
            do{
                List<Long> pendingIds = accountProcessPendingBiz.listUnMergeAccountProcessPendingId(createDates,
                        AccountProcessPendingStageEnum.PENDING.getValue(), offset, numberPerPage);
                numberCurrPage = pendingIds == null ? 0 : pendingIds.size();

                for(int i=0; i<numberCurrPage; i++){
                    Long pendingId = pendingIds.get(i);
                    boolean isSuccess = false;
                    try{
                        isSuccess = accountProcessHandler.process(pendingId);
                    }catch(Exception e){
                        logger.error("定时账务处理失败 pendingId={}", pendingId, e);
                    }

                    if(! isSuccess){ //跳过处理失败的记录，避免一直处理已经失败的记录，并且，如果一整页都失败了，会进入死循环
                        offset ++;
                    }
                }
            }while(numberCurrPage >= numberPerPage);

            logger.info("本次共处理{}条记录，成功{}条，失败{}条，耗时{}秒", totalNum, (totalNum-offset), offset, ((System.currentTimeMillis()-start)/1000));
        } catch (Exception e) {
            logger.error("定时账务处理出现异常", e);
        } finally {
            distributedLock.unlock(lock);
        }
    }

    public void scanPendingAndDoResultCallback() {
        String LockName = DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + "scheduleAccountMchCallback";
        RLock lock = distributedLock.tryLock(LockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, -1);
        try {
            //为了能使用到索引，加上createDate这个条件，为了避免在跨日时漏查数据，所以需要查询近两天内的数据
            long start = System.currentTimeMillis();
            List<Date> createDates = new ArrayList<>();
            createDates.add(new Date());
            createDates.add(DateUtil.addDay(new Date(), -1));
            int offset = 0, numberCurrPage = 0, numberPerPage = 200, totalNum = 0;

            do{
                List<Long> resultIds = accountProcessResultBiz.listAccountProcessResultId(createDates,
                        AccountProcessResultCallbackStageEnum.PENDING_SEND.getValue(), offset, numberPerPage);
                numberCurrPage = resultIds == null ? 0 : resultIds.size();

                for(int i=0; i<numberCurrPage; i++){
                    Long resultId = resultIds.get(i);
                    boolean isSuccess = false;
                    try{
                        isSuccess = accountProcessResultBiz.sendProcessResultCallbackMsg(resultId);
                    }catch(Exception e){
                        logger.error("账务结果回调异常 resultId={}", resultId, e);
                    }

                    if(! isSuccess){ //跳过处理失败的记录，避免一直处理已经失败的记录，并且，如果一整页都失败了，会进入死循环
                        offset ++;
                    }
                }
            }while(numberCurrPage >= numberPerPage);

            logger.info("本次共处理{}条记录，成功{}条，失败{}条，耗时{}秒", totalNum, (totalNum-offset), offset, ((System.currentTimeMillis()-start)/1000));
        } catch (Exception e) {
            logger.error("定时账务结果回调异常", e);
        } finally {
            distributedLock.unlock(lock);
        }
    }

    public void scanPendingAndDoMergeProcess(){
        String LockName = DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + "scheduleAccountMchMergeProcess";
        RLock lock = distributedLock.tryLock(LockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, -1);

        try {
            //为了能使用到索引，加上createDate这个条件，为了避免在跨日时漏查数据，所以需要查询近两天内的数据
            long start = System.currentTimeMillis();
            List<Date> createDates = new ArrayList<>();
            createDates.add(new Date());
            createDates.add(DateUtil.addDay(new Date(), -1));
            int offset = 0, numberCurrPage = 0, numberPerPage = 1000, totalNum = 0;

            do{
                //1. 分页查询出需要进行合并批处理的待账务处理记录
                List<PendingInfoDto> pendingInfos = accountProcessPendingBiz.listMergeAbleAccountProcessPendingInfo(
                        createDates, AccountProcessPendingStageEnum.PENDING.getValue(), offset, numberPerPage);
                numberCurrPage = pendingInfos == null ? 0 : pendingInfos.size();
                //2. 把查询出来的数据进行分组，按 accountNo + processType 分组
                Map<String, List<Long>> accountNoProcessTypeMap = groupByAccountNoAndProcessType(pendingInfos);
                //3. 逐组处理
                int failCount = processByGroup(accountNoProcessTypeMap);
                //4. 如果有失败的记录，下次分页查询跳过这些记录，避免重复处理同一笔失败的记录，不然，如果一整页的记录都失败了会另程序进入死循环
                offset += failCount;
                totalNum += numberCurrPage;
            }while(numberCurrPage >= numberPerPage);

            logger.info("本次共处理{}条记录，成功{}条，失败{}条，耗时{}秒", totalNum, (totalNum-offset), offset, ((System.currentTimeMillis()-start)/1000));
        } catch (Exception e) {
            logger.error("待账务处理记录合并批处理出现异常", e);
        } finally {
            distributedLock.unlock(lock);
        }
    }

    /**
     * 按 accountNo + processType 进行分组
     * @param pendingInfos
     */
    private Map<String, List<Long>> groupByAccountNoAndProcessType(List<PendingInfoDto> pendingInfos){
        Map<String, List<Long>> accountNoProcessTypeMap = new HashMap<>();
        if(pendingInfos == null || pendingInfos.isEmpty()){
            return accountNoProcessTypeMap;
        }

        for(PendingInfoDto pendingDto : pendingInfos){
            String key = pendingDto.getAccountNo() + "_" + pendingDto.getProcessType();
            List<Long> pendingIdList;
            if((pendingIdList = accountNoProcessTypeMap.get(key)) == null){
                pendingIdList = new ArrayList<>();
                accountNoProcessTypeMap.put(key, pendingIdList);
            }
            pendingIdList.add(pendingDto.getPendingId());
        }
        return accountNoProcessTypeMap;
    }

    /**
     * 按组处理
     * @param accountNoProcessTypeMap
     * @return  返回处理失败的个数
     */
    private int processByGroup(Map<String, List<Long>> accountNoProcessTypeMap){
        int failCount = 0;
        for(Map.Entry<String, List<Long>> entry : accountNoProcessTypeMap.entrySet()){
            String[] keyArr = entry.getKey().split("_");
            String accountNo = keyArr[0];
            Integer processType = Integer.valueOf(keyArr[1]);
            failCount += batchProcess(accountNo, processType, entry.getValue());
        }
        return failCount;
    }

    /**
     * 分批处理
     * @param accountNo
     * @param processType
     * @param pendingIds
     * @return  返回处理失败的个数
     */
    private int batchProcess(String accountNo, Integer processType, List<Long> pendingIds){
        //先对pendingIds进行分组，例如：50个为一组,分组数量的大小取决于数据库批量写入的性能瓶颈
        int numPerGroup = 100;
        Map<Integer, List<Long>> pendingIdGroupMap = ConvertUtil.splitGroup(pendingIds, numPerGroup);

        List<Long> failIdList = new ArrayList<>();
        pendingIdGroupMap.forEach((groupNum, ids) -> {
            boolean isSuccess = false;
            try{
                isSuccess = accountProcessHandler.processMerge(accountNo, ids);
            }catch(Exception e){
                logger.error("待账务处理记录批处理异常 accountNo={} processType={} pendingIds={}", accountNo, processType, JsonUtil.toJson(ids), e);
            }
            if(! isSuccess){
                failIdList.addAll(ids);
            }
        });
        if(failIdList.size() <= 0){
            return 0;
        }

        //如果有处理失败的记录，则逐条处理，这样可以避免因其中一笔请求有问题而导致同一组的其他请求都没有被处理的情况
        logger.info("accountNo={} processType={} failIdList.size={} 合并批处理失败，转为单笔处理", accountNo, processType, failIdList.size());
        AtomicInteger failCount = new AtomicInteger(0);
        failIdList.forEach(pendingId -> {
            boolean isSuccess = false;
            try{
                isSuccess = accountProcessHandler.process(pendingId);
            }catch(Exception e){
                logger.error("待账务处理记录处理失败 accountNo={} pendingId={}", accountNo, pendingId, e);
            }
            if(! isSuccess){
                failCount.incrementAndGet();
            }
        });
        return failCount.intValue();
    }
}
