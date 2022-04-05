package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.DistLockConst;
import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.accountmch.dto.AccountProcessPendingDto;
import com.xpay.facade.sequence.service.SequenceFacade;
import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.plugins.MQSender;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Description:账务处理器
 * @author: chenyf
 * @Date: 2018/3/5
 */
@Component
public class AccountProcessHandler {
    private Logger logger = LoggerFactory.getLogger(AccountProcessHandler.class);
    //每次账务处理允许的商户数
    private final static int MAX_USER_COUNT_PER_PROCESS = 2;
    //每次批量账务处理允许个数
    private final static int MAX_PROCESS_COUNT_PER_PROCESS = 50;

    @Autowired
    AccountProcessBiz accountProcessBiz;
    @Autowired
    AccountProcessPendingBiz accountProcessPendingBiz;
    @Autowired
    AccountProcessResultBiz accountProcessResultBiz;

    @Autowired
    private MQSender mqSender;
    @Autowired
    DistributedLock<RLock> distributedLock;

    @DubboReference
    SequenceFacade sequenceFacade;

    /**
     * 执行账务处理
     * @param requestDto
     * @param processDtoList
     * @return
     */
    public boolean process(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) {
        //1.参数校验
        this.validProcessParam(requestDto, processDtoList);
        //2.填充参数
        this.fillProcessParam(processDtoList);
        //3.账务处理
        return executeSingleRequest(requestDto, processDtoList);
    }

    /**
     * 执行账务处理（从待账务处理记录中拿取请求数据）
     * @param pendingRecordId
     */
    public boolean process(Long pendingRecordId){
        if(pendingRecordId == null || pendingRecordId < 0) {
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pendingRecordId+",记录Id不正确");
        }

        AccountProcessPendingDto processPending = accountProcessPendingBiz.getAccountProcessPendingById(pendingRecordId);
        if(processPending == null){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pendingRecordId+",待账务处理记录不存在");
        }else if(processPending.getProcessStage().intValue() != AccountProcessPendingStageEnum.PENDING.getValue()){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pendingRecordId+",待账务处理记录已处理，不可重复处理");
        }

        AccountRequestDto requestDto = JsonUtil.toBean(processPending.getRequestDto(), AccountRequestDto.class);
        requestDto.setPendingId(processPending.getId());

        List<AccountProcessDto> processDtoList = JsonUtil.toList(processPending.getProcessDto(), AccountProcessDto.class);
        return this.executeSingleRequest(requestDto, processDtoList);
    }

    /**
     * 合并成批量账务处理（从待账务处理记录中拿取请求数据）
     *
     * 说明：
     *    1、只允许同一个账务下的多次请求记录
     *    2、账务处理是合并成一次处理了，但是账务处理结果还是分开的，一次请求就有一个处理结果
     *    3、如果本批次处理失败了，会把待账务处理记录回退到 '待处理' 状态中
     *
     * @param accountNo
     * @param pendingIdList
     * @return
     * @throws AccountMchBizException
     */
    public boolean processMerge(String accountNo, List<Long> pendingIdList) throws AccountMchBizException {
        if (StringUtil.isEmpty(accountNo)) {
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "accountNo不能为空");
        } else if(pendingIdList == null || pendingIdList.size() <= 0) {
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "pendingIdList不能为空");
        }

        List<AccountProcessPendingDto> processPendingList = accountProcessPendingBiz.listByPendingByIdList(pendingIdList);
        if(processPendingList == null || processPendingList.size() != pendingIdList.size()) {
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "pendingIdList对应的记录不全都存在");
        }

        Map<AccountRequestDto, AccountProcessDto> requestProcessDtoMap = new LinkedHashMap<>();//保持顺序
        for (AccountProcessPendingDto pending : processPendingList) {
            if(pending.getProcessStage().intValue() != AccountProcessPendingStageEnum.PENDING.getValue()){
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pending.getId()+",待账务处理记录已处理，不可重复处理");
            }else if(pending.getMergeSupport() != PublicStatus.ACTIVE){
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pending.getId()+",当前记录不支持合并批处理！");
            }

            List<AccountProcessDto> processDtoList = JsonUtil.toList(pending.getProcessDto(), AccountProcessDto.class);
            if(processDtoList == null || processDtoList.size() != 1){
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pending.getId()+",合并批处理仅支持只个账务处理请求！");
            }

            AccountProcessDto processDto = processDtoList.get(0);
            if(! accountNo.equals(processDto.getAccountNo())){
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "pendingId="+pending.getId()+",当前记录的账户号不匹配！");
            }

            AccountRequestDto requestDto = JsonUtil.toBean(pending.getRequestDto(), AccountRequestDto.class);
            requestDto.setPendingId(pending.getId());

            requestProcessDtoMap.put(requestDto, processDto);
        }

        return executeMergeRequest(accountNo, requestProcessDtoMap);
    }

    /**
     * 缓冲异步账务处理请求（保存入MQ）
     * @param requestDto
     * @param processDtoList
     * @return
     */
    public boolean bufferAsync(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) {
        //1.参数校验
        this.validProcessParam(requestDto, processDtoList);

        //2.填充参数
        this.fillProcessParam(processDtoList);

        //3.入缓冲池，这样做的目的是避免量大的时候不至于把数据库压垮,如果量比较小，也可以直接调用 saveAsync(...) 方法入库
        return accountProcessBiz.asyncProcessBuffer(requestDto, processDtoList);
    }

    /**
     * 保存异步账务处理请求（保存入库）
     * @param requestDto
     * @param processDtoList
     */
    public boolean saveAsync(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) {
        //1.执行异步账务处理
        Long pendingRecordId = accountProcessBiz.asyncProcess(requestDto, processDtoList);

        //2.如果需要加急账务处理，则发送一条消息通知立即执行账务处理
        if (pendingRecordId != null && requestDto.isUrgent()) {
            this.notifyDoAccountProcess(pendingRecordId, getTrxNos(processDtoList));
        }
        return pendingRecordId != null;
    }

    /**
     * 记录余额快照
     * @param accountNo
     * @param snapNo
     */
    public boolean doBalanceSnap(String accountNo, String snapNo){
        RLock lock = null;
        try {
            lock = distributedLock.tryLock(getLockName(accountNo), DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);
            if(lock == null){
                logger.error("accountNo={} snapNo={} 获取账户锁失败，本次余额快照不执行！", accountNo, snapNo);
                return false;
            }
            return accountProcessBiz.doBalanceSnap(accountNo, snapNo);
        } finally {
            if(lock != null){
                //释放锁
                try {
                    distributedLock.unlock(lock);
                } catch (Throwable t) {
                }
            }
        }
    }

    /**
     * 执行垫资账户清零
     * @param accountNo
     * @param clearNo
     */
    public boolean doAdvanceClear(String accountNo, String clearNo){
        RLock lock = null;
        try {
            lock = distributedLock.tryLock(getLockName(accountNo), DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);
            if(lock == null){
                logger.error("accountNo={} clearNo={} 获取账户锁失败，本次垫资清零不执行！", accountNo, clearNo);
                return false;
            }
            return accountProcessBiz.doAdvanceClear(accountNo, clearNo);
        } finally {
            if(lock != null){
                //释放锁
                try {
                    distributedLock.unlock(lock);
                } catch (Throwable t) {
                }
            }
        }
    }

    /**
     * 校验账务处理Vo对象参数，如果有校验不通过的地方，直接抛异常
     * @param requestDto
     * @param processDtoList
     */
    private void validProcessParam(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList){
        if(requestDto == null){
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "requestDto不能为null");
        }

        //校验账务处理的业务对象是否为空
        if(processDtoList == null || processDtoList.isEmpty()){
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "processVoList不能为空");
        }else if(processDtoList.size() > MAX_PROCESS_COUNT_PER_PROCESS){
            //避免一次账务处理的时间过长
            throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "processVoList个数不能超过"+MAX_PROCESS_COUNT_PER_PROCESS);
        }

        Map<String, String> userNoMap = new HashMap<>(processDtoList.size());
        for(AccountProcessDto vo : processDtoList){
            if(StringUtil.isEmpty(vo.getTrxNo())){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "trxNo不能为空");
            }else if(vo.getProcessType() == null){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "processType不能为空");
            }else if(vo.getAmountType() == null){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "amountType不能为空");
            }else if(StringUtil.isEmpty(vo.getAccountNo())){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "accountNo不能为空");
            }else if(vo.getAmount() == null || AmountUtil.lessThan(vo.getAmount(), BigDecimal.ZERO)){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "amount不能小于0");
            }else if(vo.getFee() == null || AmountUtil.lessThan(vo.getFee(), BigDecimal.ZERO)){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "fee不能小于0");
            }else if(vo.getBussType() == null){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "bussType不能为空");
            }else if(vo.getBussCode() == null){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "bussCode不能为空");
            }

            userNoMap.put(vo.getAccountNo(), "");
            if(userNoMap.size() > MAX_USER_COUNT_PER_PROCESS){
                throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "一次处理不能超过"+MAX_USER_COUNT_PER_PROCESS+"个账户");
            }
        }
    }

    private void fillProcessParam(List<AccountProcessDto> processDtoList){
        List<Long> processIds = sequenceFacade.nextSnowId(processDtoList.size());
        for(int i=0; i<processDtoList.size(); i++){
            processDtoList.get(i).setProcessNo(processIds.get(i)+"");
        }
    }

    /**
     * 单次请求处理多条记录，支持多账户和单账户
     * @param requestDto
     * @param processDtoList
     */
    private boolean executeSingleRequest(AccountRequestDto requestDto, List<AccountProcessDto> processDtoList) throws AccountMchBizException {
        logger.info("requestDto={} processDtoList={} ", JsonUtil.toJson(requestDto), JsonUtil.toJson(processDtoList));
        String trxNos = getTrxNos(processDtoList);

        //1.如果是退回，需要找到之前扣款时的账务明细
        accountProcessBiz.findDebitDetailAndRepeatReturnCheck(processDtoList);

        //2、按用户分组
        Map<String, List<AccountProcessDto>> groupMap = this.splitGroup(processDtoList);

        //3.账务处理前置处理
        try{
            accountProcessBiz.beforeSyncProcess(requestDto, processDtoList, false);
        }catch(Throwable e){
            logger.error("trxNos={} 账务处理前发生异常", trxNos, e);
            throwBizExceptionIfNecessary(e);
            return false;
        }

        //4、给所有账户都加锁
        List<RLock> lockList = null;
        try{
            logger.info("trxNos={} 平台商户账户获取锁开始", trxNos);
            lockList = distributedLock.tryLock(getLockName(groupMap.keySet()), DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);
            logger.info("trxNos={} 平台商户账户获取锁结束 lockList.size={}", trxNos, lockList.size());
        }catch(Throwable t){
            logger.error("trxNos={} groupMap.size={} 平台商户账户获取锁出现异常", trxNos, groupMap.size(), t);
        }finally{
            if(lockList == null || lockList.size() != groupMap.size()){
                logger.error("trxNos={} groupMap.size={} lockList.size={} 平台商户账户获取锁失败", trxNos, groupMap.size(), lockList==null?0:lockList.size());
                try{
                    //回退到'待处理'
                    accountProcessBiz.beforeSyncProcess(requestDto, processDtoList, true);
                }catch(Throwable e){
                    logger.error("trxNos={} 平台商户回退到'待处理'时发生异常", trxNos, e);
                    throwBizExceptionIfNecessary(e);
                }
                throw new AccountMchBizException(AccountMchBizException.ACQUIRE_LOCK_FAIL, "获取账务锁失败");
            }
        }

        //5.账务处理中
        boolean isSuccess = false;
        Throwable exProcessing = null;
        try{
            //方法内部有做重复账务处理检测
            isSuccess = accountProcessBiz.syncProcess(groupMap);
        }catch(Throwable ex){
            exProcessing = ex;
            isSuccess = false;
            logger.error("trxNos={} 平台商户账务处理过程发生异常！", trxNos, exProcessing);
        }finally{
            //释放锁
            try{
                distributedLock.unlock(lockList);
            }catch(Throwable t){
            }
        }

        //6.账务处理后
        Long processResultId = null;
        try{
            processResultId = accountProcessBiz.afterSyncProcess(requestDto, processDtoList, isSuccess, exProcessing);
        }catch (Throwable exAfter){
            logger.error("trxNos={} 平台商户账务处理后发生异常", trxNos, exAfter);
        }

        //7.如果需要回调并且是加急回调，则通过MQ通知立即执行账务处理回调
        if(requestDto.isUrgent()){
            this.notifyDoAccountProcessCallBack(processResultId, getTrxNos(processDtoList));
        }
        this.throwBizExceptionIfNecessary(exProcessing);
        return isSuccess;
    }

    /**
     * 合并单次请求为一次处理，支持单账户
     * @param accountNo
     * @param requestProcessDtoMap
     * @return
     */
    private boolean executeMergeRequest(String accountNo, Map<AccountRequestDto, AccountProcessDto> requestProcessDtoMap){
        //1.参数准备
        List<Long> pendingIdList = new ArrayList<>(requestProcessDtoMap.size());
        List<AccountProcessDto> processDtoList = new ArrayList<>(requestProcessDtoMap.size());
        requestProcessDtoMap.forEach((k,v) -> {
            pendingIdList.add(k.getPendingId());
            processDtoList.add(v);
        });

        String trxNos = getTrxNos(processDtoList);

        //2.如果是退回，需要找到之前扣款时的账务明细
        accountProcessBiz.findDebitDetailAndRepeatReturnCheck(processDtoList);

        //3.把待账务处理记录更新为'处理中'
        try{
            accountProcessBiz.beforeSyncProcessMerge(pendingIdList, false);
        }catch(Throwable e){
            logger.error("trxNos={} 账务处理前发生异常", trxNos, e);
            return false;
        }

        //4、给所有账户都加锁
        RLock lock = null;
        try{
            logger.info("accountNo={} trxNos={} 平台商户账户获取锁开始", accountNo, trxNos);
            lock = distributedLock.tryLock(getLockName(accountNo), DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);
            logger.info("accountNo={} trxNos={} 平台商户账户获取锁结束", accountNo, trxNos);
        }catch(Throwable t){
            logger.error("accountNo={} trxNos={} 平台商户账户获取锁出现异常", accountNo, trxNos, t);
        }finally{
            if(lock == null){
                logger.error("accountNo={} trxNos={} 平台商户账户获取锁失败", accountNo, trxNos);
                try{
                    //回退到'待处理'
                    accountProcessBiz.beforeSyncProcessMerge(pendingIdList, true);
                }catch(Throwable e){
                    logger.error("accountNo={} trxNos={} 平台商户回退到'待处理'时发生异常", accountNo, trxNos, e);
                }
                return false;
            }
        }

        Map<String, List<AccountProcessDto>> groupMap = new HashMap<>();
        groupMap.put(accountNo, processDtoList);

        //5.账务处理中
        boolean isSuccess = false;
        Throwable exProcessing = null;
        try{
            //方法内部有做重复账务处理检测
            isSuccess = accountProcessBiz.syncProcess(groupMap);
        }catch(Throwable ex){
            exProcessing = ex;
            isSuccess = false;
            logger.error("accountNo={} trxNos={} 平台商户账务处理过程发生异常！", accountNo, trxNos, exProcessing);
        }finally{
            //释放锁
            try{
                distributedLock.unlock(lock);
            }catch(Throwable t){
            }
        }

        //6.账务处理后
        try{
            accountProcessBiz.afterSyncProcessMerge(requestProcessDtoMap, isSuccess, exProcessing);
        }catch (Throwable exAfter){
            logger.error("accountNo={} trxNos={} errorMsg={} 平台商户账务处理后发生异常", accountNo, trxNos, exAfter.getMessage());
        }

        return isSuccess;
    }

    /**
     * 异常抛出转换
     * @param ex
     */
    private void throwBizExceptionIfNecessary(Throwable ex){
        if(ex != null){
            if(ex instanceof AccountMchBizException){
                throw (AccountMchBizException) ex;
            }else if(ex instanceof BizException){
                throw (BizException) ex;
            }else{
                throw new BizException(AccountMchBizException.UNEXPECT_ERROR, "UNKNOWN_SYSTEM_ERROR");
            }
        }
    }

    /**
     * 通知进行账务处理
     */
    private void notifyDoAccountProcess(Long processPendingId, String trxNos){
        logger.info("processPendingId={}", processPendingId);
        if(processPendingId == null) {
            return;
        }

        try{
            Map<String, Long> idMap = new HashMap<>();
            idMap.put("processPendingId", processPendingId);

            MsgDto msgDto = new MsgDto();
            msgDto.setTopic(TopicDest.ACCOUNT_MCH_URGENT_PROCESS);
            msgDto.setTags(TopicGroup.ACCOUNT_MCH_GROUP);
            msgDto.setTrxNo(StringUtil.subLeft(trxNos, 32));
            msgDto.setJsonParam(JsonUtil.toJson(idMap));

            mqSender.sendOneAsync(msgDto, null);
            logger.info("trxNos={} 通知进行账务处理完毕", trxNos);
        }catch(Throwable e){
            logger.error("trxNos={} 通知进行账务处理时出现异常", trxNos, e);
        }
    }

    /**
     * 通知进行账务处理结果回调
     * @param processResultId
     * @param trxNos
     */
    private void notifyDoAccountProcessCallBack(Long processResultId, String trxNos){
        if(processResultId == null) {
            return;
        }

        try{
            Map<String, Long> idMap = new HashMap<>();
            idMap.put("processResultId", processResultId);

            MsgDto msgDto = new MsgDto();
            msgDto.setTopic(TopicDest.ACCOUNT_MCH_URGENT_CALLBACK);
            msgDto.setTags(TopicGroup.ACCOUNT_MCH_GROUP);
            msgDto.setTrxNo(StringUtil.subLeft(trxNos, 32));
            msgDto.setJsonParam(JsonUtil.toJson(idMap));

            mqSender.sendOneAsync(msgDto, null);
            logger.info("processResultId={} trxNos={} 通知进行账务处理结果回调完毕", processResultId, trxNos);
        }catch(Throwable e){
            logger.error("trxNos={} 通知账务处理结果回调时出现异常", trxNos, e);
        }
    }

    /**
     * 对processVoList根据商户号分组
     * @param processDtoList
     * @return
     */
    private Map<String, List<AccountProcessDto>> splitGroup(List<AccountProcessDto> processDtoList){
        Map<String, List<AccountProcessDto>> groupMap = new LinkedHashMap<>();//保持顺序
        for (AccountProcessDto processDto : processDtoList) {
            if (! groupMap.containsKey(processDto.getAccountNo())) {
                groupMap.put(processDto.getAccountNo(), new ArrayList<>());
            }
            groupMap.get(processDto.getAccountNo()).add(processDto);
        }
        return groupMap;
    }

    public static String getLockName(String accountNo){
        return DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + accountNo;
    }

    private static Set<String> getLockName(Set<String> accountNos) {
        Set<String> lockNames = new HashSet<>(accountNos.size());
        for (String userNo : accountNos) {
            lockNames.add(DistLockConst.ACCOUNT_MCH_LOCK_PREFIX + userNo);
        }
        return lockNames;
    }

    private String getTrxNos(List<AccountProcessDto> processDtoList){
        if(processDtoList.size() == 1){
            return processDtoList.get(0).getTrxNo();
        }
        List<String> trxNos = new ArrayList<>(processDtoList.size());
        processDtoList.forEach(e -> trxNos.add(e.getTrxNo()));
        return JsonUtil.toJson(trxNos);
    }
}
