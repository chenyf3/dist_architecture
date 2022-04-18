package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.dto.account.AccountDetailDto;
import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.statics.enums.account.AccountProcessResultCallbackStageEnum;
import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.accountmch.dto.AccountProcessBufferDto;
import com.xpay.service.accountmch.accounting.AccountingHelper;
import com.xpay.service.accountmch.accounting.processors.AccountingProcessor;
import com.xpay.service.accountmch.accounting.AccountingStrategy;
import com.xpay.service.accountmch.bo.AccountingBo;
import com.xpay.service.accountmch.dao.AccountMchDao;
import com.xpay.service.accountmch.dao.AccountProcessDetailDao;
import com.xpay.service.accountmch.dao.AccountProcessDetailHistoryDao;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessPending;
import com.xpay.service.accountmch.entity.AccountProcessResult;
import com.xpay.starter.plugin.plugins.MQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description:账务处理业务逻辑层
 *
 * @author: chenyf
 * @Date: 2018/3/5
 */
@Component
public class AccountProcessBiz {
    private Logger logger = LoggerFactory.getLogger(AccountProcessBiz.class);

    @Autowired
    AccountMchDao accountMchDao;
    @Autowired
    AccountProcessDetailDao accountProcessDetailDao;
    @Autowired
    AccountProcessDetailHistoryDao accountProcessDetailHistoryDao;

    @Autowired
    AccountProcessPendingBiz accountProcessPendingBiz;
    @Autowired
    AccountProcessResultBiz accountProcessResultBiz;
    @Autowired
    AccountingStrategy accountingStrategy;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    MQSender mqSender;

    /**
     * 缓冲异步账务请求，此处使用的MQ作为缓冲器，需要做好MQ的高可用，并且处理好消息丢失问题
     * @param requestDto
     * @param processDtoList
     * @return
     */
    public boolean asyncProcessBuffer(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) {
        AccountProcessBufferDto bufferDto = new AccountProcessBufferDto();
        bufferDto.setRequestDto(requestDto);
        bufferDto.setProcessDtoList(processDtoList);
        bufferDto.setTopic(TopicDest.ACCOUNT_MCH_PROCESS_BUFFER);
        bufferDto.setTags(TopicGroup.ACCOUNT_MCH_GROUP);
        bufferDto.setTrxNo(processDtoList.get(0).getTrxNo());//只拿第一个，因为消费端不需要此参数值
        bufferDto.setMchNo(processDtoList.get(0).getAccountNo());//只拿第一个，因为消费端不需要此参数值
        mqSender.sendOne(bufferDto, (msgDto) -> {
            logger.warn("缓冲异步账务发送MQ失败，将直接入库处理");
            try {
                //如果MQ发送失败，则直接入库处理
                asyncProcess(requestDto, processDtoList);
            } catch (Exception e){
                //万一还是失败了，则还可以通过日志找回
                logger.error("缓冲异步账务入库失败 AccountProcessBufferDto:{}", JsonUtil.toJson(msgDto));
            }
        });
        return true;
    }

    /**
     * 异步账务处理
     * @param requestDto
     * @param processDtoList
     */
    public Long asyncProcess(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList){
        //加急或多笔的，不能执行合并批处理
        boolean isMergeSupport = true;
        if(requestDto.isUrgent() || processDtoList.size() > 1){
            isMergeSupport = false;
        }

        AccountProcessPending processPending = new AccountProcessPending();
        processPending.setCreateTime(new Date());
        processPending.setCreateDate(processPending.getCreateTime());
        processPending.setModifyTime(processPending.getCreateTime());
        processPending.setProcessStage(AccountProcessPendingStageEnum.PENDING.getValue());
        processPending.setMergeSupport(isMergeSupport ? PublicStatus.ACTIVE : PublicStatus.INACTIVE);
        processPending.setRequestDto(JsonUtil.toJsonFriendlyNotNull(requestDto));
        processPending.setProcessDto(JsonUtil.toJsonFriendlyNotNull(processDtoList));
        processPending.setRemark("");//预留字段

        try{
            accountProcessPendingBiz.add(processPending);
            return processPending.getId();
        }catch(Throwable e){
            logger.error("异步账务处理出现异常", e);
            throw new AccountMchBizException(AccountMchBizException.UNEXPECT_ERROR, e.getMessage());
        }
    }

    /**
     * 账务处理前置逻辑
     * @param requestDto
     * @param processDtoList
     */
    public void beforeSyncProcess(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList, boolean isRevert){
        boolean isFromAsync = requestDto.getPendingId() != null && requestDto.getPendingId() > 0;
        if(! isFromAsync){
            return;
        }

        if(isRevert){
            //更新待处理表状态回退到"待处理"
            accountProcessPendingBiz.updatePendingStatus(requestDto.getPendingId(), AccountProcessPendingStageEnum.PENDING, AccountProcessPendingStageEnum.PROCESSING);
        }else{
            //更新待处理表状态为"处理中"
            accountProcessPendingBiz.updatePendingStatus(requestDto.getPendingId(), AccountProcessPendingStageEnum.PROCESSING, AccountProcessPendingStageEnum.PENDING);
        }
    }

    /**
     * 同步账务处理
     * 注意：本方法使用数据库乐观锁来保障并发时的数据安全问题，所以，如果想要降低处理失败的概率，需调用方自行使用Redis等分布式悲观锁
     *
     * @param userNoProcessDtoMap
     */
    public boolean syncProcess(Map<String, List<AccountProcessDto>> userNoProcessDtoMap){
        Date accountingTime = DateUtil.getDateWithoutMills(new Date());//记账时间，同一批次下的请求，使用同一个记账时间
        AccountingBo accountingBo = new AccountingBo();

        for (Map.Entry<String, List<AccountProcessDto>> entry : userNoProcessDtoMap.entrySet()) {
            String accountNo = entry.getKey();
            List<AccountProcessDto> processDtoList = entry.getValue();
            //1.账户数据获取 & 账户存在性校验
            AccountMch account = accountMchDao.getByAccountNo(accountNo);
            if(account == null){
                throw new AccountMchBizException(AccountMchBizException.ACCOUNT_RECORD_NOT_EXIT, accountNo + "账户记录不存在");
            }

            /**
             * 确保一个商户下的同一批次里边只在第一笔执行了快照或垫资清零，因为，假设总共有5笔，在第3笔时进行了快照或垫资清零，
             * 但在第5笔时抛异常了，这种情况对余额快照的影响是导致快照的金额不准确，而对垫资账户来说影响就更大了，因为前面2笔
             * 对AccountMch的计算在垫资清零时保存到了数据库中，而实际上要求本次的5条都需要回滚，这就造成了数据不一致的情况
             */
            //2.如有需要，则执行账户余额快照和垫资账户清零
            boolean isDoSnap = accountingHelper.doBalanceSnap(accountingTime.getTime(), account, processDtoList.get(0).getProcessNo());
            boolean isDoClear = accountingHelper.doAdvanceClear(accountingTime.getTime(), account, processDtoList.get(0).getProcessNo());

            //3.如果有执行快照或垫资清零，则重新获取账户记录
            if(isDoSnap || isDoClear){
                account = accountMchDao.getByAccountNo(accountNo);
            }

            //4.执行账务处理的逻辑计算
            for(AccountProcessDto processDto : processDtoList) {
                //4.1 初始化一些参数值
                processDto.setAccountTime(accountingTime);
                //4.2 选择相应的账务处理器
                AccountingProcessor processor = accountingStrategy.getProcessor(processDto.getProcessType());
                //4.3 执行账务处理计算
                processor.process(account, processDto, accountingBo);
            }
        }

        //5.保存账务处理结果（需要确保所有账户、所有明细都在一个事务内）
        accountingHelper.saveAccountingResult(accountingBo);
        return true;
    }

    /**
     * 账务处理后置逻辑
     * @param requestDto
     * @param processDtoList
     * @param isSuccess
     * @param ex
     */
    @Transactional(rollbackFor = Exception.class)
    public Long afterSyncProcess(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList, boolean isSuccess, Throwable ex){
        boolean isFromAsync = requestDto.getPendingId() != null && requestDto.getPendingId() > 0;
        Integer errorCode = getErrorCode(ex);
        String errorMsg = getErrorMsg(ex);
        List<String> trxNos = new ArrayList<>(processDtoList.size());
        for (AccountProcessDto processDto : processDtoList) {
            trxNos.add(processDto.getTrxNo());
            processDto.setAccountDetailDto(null);
        }

        Integer callbackStage;
        if (this.isNeedAudit(isSuccess, isFromAsync, errorCode)) {
            callbackStage = AccountProcessResultCallbackStageEnum.PENDING_AUDIT.getValue();
        } else if (StringUtil.isEmpty(requestDto.getCallbackQueue())) {
            callbackStage = AccountProcessResultCallbackStageEnum.NONE_SEND.getValue();
        } else {
            callbackStage = AccountProcessResultCallbackStageEnum.PENDING_SEND.getValue();
        }

        AccountProcessResult accountProcessResult = new AccountProcessResult();
        accountProcessResult.setCreateTime(new Date());
        accountProcessResult.setCreateDate(accountProcessResult.getCreateTime());
        accountProcessResult.setProcessResult(isSuccess ? PublicStatus.ACTIVE : PublicStatus.INACTIVE);
        accountProcessResult.setErrCode(errorCode);
        accountProcessResult.setErrMsg(errorMsg);
        accountProcessResult.setRemark("");
        accountProcessResult.setIsFromAsync(isFromAsync ? PublicStatus.ACTIVE : PublicStatus.INACTIVE);
        accountProcessResult.setCallbackStage(callbackStage);
        accountProcessResult.setRequestDto(JsonUtil.toJsonFriendlyNotNull(requestDto));
        accountProcessResult.setProcessDto(JsonUtil.toJsonFriendlyNotNull(processDtoList));

        try{
            //如果是异步处理的，则把待账物处理表的状态更新为"已处理"
            if (isFromAsync) {
                accountProcessPendingBiz.updatePendingStatus(requestDto.getPendingId(), AccountProcessPendingStageEnum.FINISHED, AccountProcessPendingStageEnum.PROCESSING);
            }

            //写入账务处理结果表
            accountProcessResultBiz.add(accountProcessResult);
        }catch(Throwable e){
            //把账务处理结果整个实体打印到日志中，可以通过日志人工录入到数据库中
            logger.error("processResultSaveFail 账务处理结果保存失败 AccountProcessResult={}", JsonUtil.toJson(accountProcessResult), e);
            throw e;//抛出异常让事务回滚
        }

        return accountProcessResult.getId();
    }

    /**
     * 账务批处理前置逻辑
     * @param pendingRecordIds
     * @param isRevert
     */
    public void beforeSyncProcessMerge(List<Long> pendingRecordIds, boolean isRevert){
        if(isRevert){
            //更新待处理表状态回退到"待处理"
            accountProcessPendingBiz.updatePendingStatus(pendingRecordIds, AccountProcessPendingStageEnum.PENDING, AccountProcessPendingStageEnum.PROCESSING);
        }else{
            //更新待处理表状态为"处理中"
            accountProcessPendingBiz.updatePendingStatus(pendingRecordIds, AccountProcessPendingStageEnum.PROCESSING, AccountProcessPendingStageEnum.PENDING);
        }
    }

    /**
     * 账务批处理后置逻辑
     * @param requestProcessDtoMap
     * @param isSuccess
     * @param ex
     */
    @Transactional(rollbackFor = Exception.class)
    public void afterSyncProcessMerge(Map<AccountRequestDto, AccountProcessDto> requestProcessDtoMap, boolean isSuccess, Throwable ex){
        if(! isSuccess){
            //如果是处理失败，则把待账务处理记录回退到 '待处理' 状态
            List<Long> pendingRecordIds = new ArrayList<>();
            for(Map.Entry<AccountRequestDto, AccountProcessDto> entry : requestProcessDtoMap.entrySet()){
                pendingRecordIds.add(entry.getKey().getPendingId());
            }
            beforeSyncProcessMerge(pendingRecordIds, true);
            return;
        }

        boolean isFromAsync = true;
        Integer errorCode = getErrorCode(null);
        String errorMsg = getErrorMsg(null);
        Integer resultValue = PublicStatus.ACTIVE;

        List<Long> pendingRecordIds = new ArrayList<>();
        List<AccountProcessResult> processResultList = new ArrayList<>();
        for(Map.Entry<AccountRequestDto, AccountProcessDto> entry : requestProcessDtoMap.entrySet()){
            AccountRequestDto requestDto = entry.getKey();
            AccountProcessDto processDto = entry.getValue();
            List<AccountProcessDto> processDtoList = Collections.singletonList(processDto);

            AccountProcessResult processResult = new AccountProcessResult();
            processResult.setCreateTime(new Date());
            processResult.setCreateDate(processResult.getCreateTime());
            processResult.setProcessResult(resultValue);
            processResult.setErrCode(errorCode);
            processResult.setErrMsg(errorMsg);
            processResult.setRemark("");
            processResult.setIsFromAsync(isFromAsync ? PublicStatus.ACTIVE : PublicStatus.INACTIVE);
            processResult.setCallbackStage(AccountProcessResultCallbackStageEnum.PENDING_SEND.getValue());
            processResult.setRequestDto(JsonUtil.toJsonFriendlyNotNull(requestDto));
            processResult.setProcessDto(JsonUtil.toJsonFriendlyNotNull(processDtoList));

            processResultList.add(processResult);
            pendingRecordIds.add(requestDto.getPendingId());
        }

        try{
            //把待账物处理表的状态更新为"已处理"
            accountProcessPendingBiz.updatePendingStatus(pendingRecordIds, AccountProcessPendingStageEnum.FINISHED, AccountProcessPendingStageEnum.PROCESSING);

            //写入账务处理结果表
            accountProcessResultBiz.add(processResultList);
        }catch(Throwable e){
            //把账务处理结果整个实体打印到日志中，可以通过日志人工录入到数据库中
            logger.error("账务处理结果保存出现异常 processResultList={}", JsonUtil.toJson(processResultList), e);
            throw e;
        }
    }

    /**
     * 记录余额快照
     * @param accountNo
     * @param snapNo
     */
    public boolean doBalanceSnap(String accountNo, String snapNo){
        AccountMch account = accountMchDao.getByAccountNo(accountNo);
        if(account == null){
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_RECORD_NOT_EXIT, accountNo + "账户记录不存在");
        }
        return accountingHelper.doBalanceSnap(System.currentTimeMillis(), account, snapNo);
    }

    /**
     * 执行垫资账户清零
     * @param accountNo
     * @param clearNo
     */
    public boolean doAdvanceClear(String accountNo, String clearNo){
        AccountMch account = accountMchDao.getByAccountNo(accountNo);
        if(account == null){
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_RECORD_NOT_EXIT, accountNo + "账户记录不存在");
        }
        return accountingHelper.doAdvanceClear(System.currentTimeMillis(), account, clearNo);
    }

    /**
     * 查找扣款时的账务明细，如果发现某笔订单已经退回过，则直接抛出异常
     * @param processDtoList
     */
    public void findDebitDetailAndRepeatReturnCheck(List<AccountProcessDto> processDtoList){
        for(AccountProcessDto processDto : processDtoList){
            //只有扣款退回时，才需要查找账务明细
            if (processDto.getProcessType() != AccountProcessTypeEnum.DEBIT_RETURN.getValue()) {
                continue;
            }

            String accountNo = processDto.getAccountNo();
            String requestNo = processDto.getTrxNo();

            List<AccountDetailDto> detailDtoList = accountProcessDetailDao.listDetailDtoByAccountNoAndRequestNo(accountNo, requestNo);
            if(detailDtoList == null || detailDtoList.isEmpty()){
                detailDtoList = accountProcessDetailHistoryDao.listDetailDtoByAccountNoAndRequestNo(accountNo, requestNo, DateUtil.addDay(new Date(), -180));//只查询半年内的
            }
            if(detailDtoList == null || detailDtoList.isEmpty()){
                logger.info("accountNo={} requestNo={} 两个账户明细都没有获取到扣款记录", accountNo, requestNo);
                continue;
            }

            for(AccountDetailDto detailDto : detailDtoList){
                if(AccountProcessTypeEnum.DEBIT_OUT.getValue() == detailDto.getProcessType()){
                    processDto.setAccountDetailDto(detailDto);
                }else if(AccountProcessTypeEnum.DEBIT_RETURN.getValue() == detailDto.getProcessType()){
                    //说明当前 accountNo + trxNo + processType 已经进行过退回，则不能再处理退回
                    throw new AccountMchBizException(AccountMchBizException.ACCOUNT_PROCESS_REPEAT, requestNo+"勿重复退回");
                }
            }
        }
    }

    /**
     * 根据异常信息获取错误码
     * @param ex
     * @return
     */
    private int getErrorCode(Throwable ex){
        if(ex == null){
            //无异常
            return 0;
        }else if(ex instanceof BizException){
            //业务异常
            return ((BizException) ex).getCode();
        }else{
            //系统异常
            return 1;
        }
    }

    /**
     * 根据异常信息获取错误描述信息
     * @param ex
     * @return
     */
    private String getErrorMsg(Throwable ex){
        if(ex == null){
            //无异常
            return "";
        }else if(ex instanceof BizException){
            //业务异常
            return ((BizException) ex).getMsg();
        }else{
            //系统异常，截取前500个字符，因为数据库中只保存500
            String msg = StringUtil.subLeft(ex.toString(), 500);
            return msg;
        }
    }

    /**
     * 账务处理结果是否需要审核
     * @param isSuccess
     * @param isFromAsync
     * @param errorCode
     * @return
     */
    private boolean isNeedAudit(boolean isSuccess, boolean isFromAsync, int errorCode){
        if (isSuccess || isFromAsync==false) {
            //处理成功、同步账务处理，都不需要审核
            return false;
        }

        if (errorCode == AccountMchBizException.TOTAL_BALANCE_NOT_ENOUGH
                || errorCode == AccountMchBizException.SETTLED_AMOUNT_NOT_ENOUGH
                || errorCode == AccountMchBizException.UNSETTLE_AMOUNT_NOT_ENOUGH
                || errorCode == AccountMchBizException.AVAIL_ADVANCE_AMOUNT_NOT_ENOUGH
                || errorCode == AccountMchBizException.AVAIL_BALANCE_NOT_ENOUGH
                || errorCode == AccountMchBizException.AVAIL_BALANCE_NOT_ENOUGH_FOR_RCMS
                || errorCode == AccountMchBizException.DEBIT_ABLE_AMOUNT_NOT_ENOUGH) {
            //对于各种余额不足的业务异常，不需要审核
            return false;
        } else if (errorCode == AccountMchBizException.ACCOUNT_RECORD_NOT_EXIT
                || errorCode == AccountMchBizException.ACCOUNT_STATUS_IS_INACTIVE) {
            //对于一些可以确定账务处理一定是失败的，并且是不再需要审核为重新账务处理的，都不需要审核
            return false;
        } else if (errorCode > 0) {
            //其他异常，一律要审核，其中，errorCode等于1时，表示系统异常，其他值则表示业务异常
            return true;
        } else {
            return false;
        }
    }
}
