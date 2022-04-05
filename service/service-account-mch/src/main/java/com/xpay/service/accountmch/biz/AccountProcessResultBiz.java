package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.statics.enums.account.AccountProcessResultCallbackStageEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.accountmch.dto.AccountProcessResultDto;
import com.xpay.service.accountmch.dao.AccountProcessResultDao;
import com.xpay.service.accountmch.dao.AccountProcessResultHistoryDao;
import com.xpay.service.accountmch.entity.AccountProcessResult;
import com.xpay.starter.plugin.plugins.MQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description:账务处理结果逻辑处理层
 * @author: chenyf
 * @Date: 2018/3/8
 */
@Component
public class AccountProcessResultBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountProcessPendingBiz accountProcessPendingBiz;
    @Autowired
    AccountQueryBiz accountQueryBiz;
    @Autowired
    private AccountProcessResultDao accountProcessResultDao;
    @Autowired
    AccountProcessResultHistoryDao accountProcessResultHistoryDao;

    @Autowired
    private MQSender mqSender;

    public long add(AccountProcessResult accountProcessResult){
        accountProcessResultDao.insert(accountProcessResult);
        return accountProcessResult.getId();
    }

    public void add(List<AccountProcessResult> processResultList){
        accountProcessResultDao.insert(processResultList);
    }

    /**
     * 根据账务处理明细来审核账务处理结果
     * @param id
     * @param reprocessWhenFail   如果账务处理失败，是否重新执行账务处理(仅对异步账务处理有效)
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean checkProcessDetailToAuditProcessResult(Long id, boolean reprocessWhenFail) {
        if (id == null) {
            return false;
        }

        //1.检查账务处理结果是否存在，以及状态是否处于 '待审核'
        AccountProcessResult processResult = getAccountProcessResultById(id);
        if(processResult == null) {
            return false;
        } else if(processResult.getCallbackStage() != AccountProcessResultCallbackStageEnum.PENDING_AUDIT.getValue()) {
            return false;
        }

        //2.根据账务明细是否存在来判断请求数据是否有处理成功
        int processDetailExistCount = 0;
        List<AccountProcessDto> processDtoList = JsonUtil.toList(processResult.getProcessDto(), AccountProcessDto.class);
        for(AccountProcessDto processDto : processDtoList) {
            String accountNo = processDto.getAccountNo();
            String requestNo = processDto.getTrxNo();
            Integer processType = processDto.getProcessType();
            String processNo = processDto.getProcessNo();
            boolean isExist = accountQueryBiz.isAccountProcessDetailExist(accountNo, requestNo, processType, processNo);
            if(isExist){
                processDetailExistCount ++;
            }
        }
        if(processDetailExistCount == processDtoList.size()){ //表示全部处理成功
            processResult.setProcessResult(PublicStatus.ACTIVE);
            processResult.setRemark("都有账务明细，审核为处理成功");
        }else if(processDetailExistCount == 0){ //表示全部处理失败
            processResult.setProcessResult(PublicStatus.INACTIVE);
            processResult.setRemark("无账务明细，审核为处理失败");
        }else{ //账务处理时需要保证到同一次请求的，要么同时成功，要么同时失败，只有部分成功的，一定是有其他问题了
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "部分请求有账务明细，部分请求没有账务明细，不正常！");
        }

        //3.根据处理结果是成功还是失败来设置回调状态的值
        AccountRequestDto requestDto = JsonUtil.toBean(processResult.getRequestDto(), AccountRequestDto.class);
        boolean isNeedCallBack = StringUtil.isNotEmpty(requestDto.getCallbackQueue());
        boolean isNeedReprocess = false;
        if(processResult.getProcessResult() == PublicStatus.INACTIVE && reprocessWhenFail){ //需要重新处理
            if(processResult.getIsFromAsync() == PublicStatus.ACTIVE){ //只有异步账务处理请求才能重新账务处理
                isNeedCallBack = false;
                isNeedReprocess = true;
                processResult.setCallbackStage(AccountProcessResultCallbackStageEnum.NONE_SEND.getValue());
            }else if(isNeedCallBack){
                processResult.setCallbackStage(AccountProcessResultCallbackStageEnum.SENT.getValue());
            }else{
                processResult.setCallbackStage(AccountProcessResultCallbackStageEnum.NONE_SEND.getValue());
            }
        }else{
            if(isNeedCallBack){
                processResult.setCallbackStage(AccountProcessResultCallbackStageEnum.SENT.getValue());
            }else{
                processResult.setCallbackStage(AccountProcessResultCallbackStageEnum.NONE_SEND.getValue());
            }
        }

        //4.更新账务处理结果
        accountProcessResultDao.update(processResult);

        //5.如果需要重新处理，则把待账务处理记录更新为 '待处理' 状态
        if(isNeedReprocess){
            Long pendingId = requestDto.getPendingId();
            AccountProcessPendingStageEnum stageNew = AccountProcessPendingStageEnum.PENDING;
            AccountProcessPendingStageEnum stageOld = AccountProcessPendingStageEnum.FINISHED;
            accountProcessPendingBiz.updatePendingStatus(pendingId, stageNew, stageOld);
        }

        //6.如果需要发送回调，则立即发送
        if(isNeedCallBack){
            com.xpay.common.statics.dto.account.AccountProcessResultDto resultDto = buildProcessResultDto(processResult);
            if(resultDto != null && StringUtil.isNotEmpty(resultDto.getTopic())){
                mqSender.sendOne(resultDto);
            }
        }
        return true;
    }

    /**
     * 发送处理结果的回调通知，然后更新为 “已发送”
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendProcessResultCallbackMsg(Long id){
        logger.info("id={}", id);
        if(id == null){
            return false;
        }

        boolean isSentStage = false;//是否记录已经是'已发送'状态
        AccountProcessResult result = getAccountProcessResultById(id);
        if(result == null){
            return false;
        }else if(result.getCallbackStage().intValue() == AccountProcessResultCallbackStageEnum.PENDING_AUDIT.getValue()){
            //处于"待审核"状态的不能发送
            return false;
        }else if(result.getCallbackStage().intValue() == AccountProcessResultCallbackStageEnum.NONE_SEND.getValue()){
            //处于"不发送"状态的不能发送
            return false;
        }else if(result.getCallbackStage().intValue() == AccountProcessResultCallbackStageEnum.SENT.getValue()){
            isSentStage = true;
        }

        //把当前记录的发送阶段更新为 "已发送"
        if (! isSentStage){
            result.setCallbackStage(AccountProcessResultCallbackStageEnum.SENT.getValue());
            accountProcessResultDao.update(result);
        }

        //如果需要回调，则封装回调参数并发送回调
        com.xpay.common.statics.dto.account.AccountProcessResultDto resultDto = buildProcessResultDto(result);
        if(resultDto == null || StringUtil.isEmpty(resultDto.getTopic())){
            return true;
        }
        boolean isSuccess = mqSender.sendOne(resultDto);
        if(isSuccess){
            logger.info("id={} 账务处理结果回调成功", result.getId());
            return true;
        }else{
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "账务处理结果回调发送失败");
        }
    }

    /**
     * 添加账务处理结果，当出现一些比较极端的情况(如：数据库连接池不足等)，导致账务处理完成，但是没有生成账务处理结果时，可使用
     * @param processResult
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addAccountProcessResult(AccountProcessResultDto processResult){
        if(processResult == null){
            return false;
        }

        //如果是异步账务处理，则把待账务处理记录从处理中更新为已处理
        if(processResult.getIsFromAsync().equals(PublicStatus.ACTIVE)){
            AccountRequestDto requestDto = JsonUtil.toBean(processResult.getRequestDto(), AccountRequestDto.class);
            accountProcessPendingBiz.updatePendingStatus(requestDto.getPendingId(),
                    AccountProcessPendingStageEnum.FINISHED, AccountProcessPendingStageEnum.PROCESSING);
        }

        AccountProcessResult result = BeanUtil.newAndCopy(processResult, AccountProcessResult.class);
        accountProcessResultDao.insert(result);
        return true;
    }

    public PageResult<List<AccountProcessResultDto>> listByPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<AccountProcessResult>> result = accountProcessResultDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AccountProcessResultDto.class), result);
    }

    public PageResult<List<AccountProcessResultDto>> listHistoryByPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<AccountProcessResult>> result = accountProcessResultHistoryDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AccountProcessResultDto.class), result);
    }

    public AccountProcessResult getAccountProcessResultById(Long id){
        if(id == null){
            return null;
        }
        AccountProcessResult result = accountProcessResultDao.getById(id);
        if(result != null){
            return result;
        }
        return accountProcessResultHistoryDao.getById(id);
    }

    public AccountProcessResultDto getAccountProcessResultDtoById(Long id){
        AccountProcessResult result = getAccountProcessResultById(id);
        return BeanUtil.newAndCopy(result, AccountProcessResultDto.class);
    }

    /**
     * 分页查询账务处理结果的Id
     * @param status
     * @param offset
     * @param limit
     * @return
     */
    public List<Long> listAccountProcessResultId(List<Date> createDates, int status, int offset, int limit){
        if(createDates== null || createDates.isEmpty() || offset < 0 || limit <= 0){
            return null;
        }

        List<String> createDateStrList = new ArrayList<>(createDates.size());
        createDates.forEach(date -> createDateStrList.add(DateUtil.formatDate(date)));

        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("createDateList", createDateStrList);
        paramMap.put("sendMsgStage", status);
        return accountProcessResultDao.listAccountProcessResultId(paramMap, offset, limit);
    }

    public int countNeedAuditRecord(List<Date> createDates){
        List<String> createDateStrList = new ArrayList<>(createDates.size());
        createDates.forEach(date -> createDateStrList.add(DateUtil.formatDate(date)));
        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("createDateList", createDateStrList);
        paramMap.put("sendMsgStage", AccountProcessResultCallbackStageEnum.PENDING_AUDIT.getValue());
        return (int) accountProcessResultDao.countBy(paramMap);
    }

    private com.xpay.common.statics.dto.account.AccountProcessResultDto buildProcessResultDto(AccountProcessResult result){
        AccountRequestDto requestDto = JsonUtil.toBean(result.getRequestDto(), AccountRequestDto.class);
        if(StringUtil.isEmpty(requestDto.getCallbackQueue())){
            return null;
        }

        List<com.xpay.common.statics.dto.account.AccountProcessResultDto.RequestInfo> requestInfos = new ArrayList<>();
        List<AccountProcessDto> processVoList = JsonUtil.toList(result.getProcessDto(), AccountProcessDto.class);
        for(AccountProcessDto processDto : processVoList){
            com.xpay.common.statics.dto.account.AccountProcessResultDto.RequestInfo requestInfo = new com.xpay.common.statics.dto.account.AccountProcessResultDto.RequestInfo();
            requestInfo.setAccountNo(processDto.getAccountNo());
            requestInfo.setTrxNo(processDto.getTrxNo());
            requestInfo.setProcessType(processDto.getProcessType());
            requestInfo.setAccountTime(processDto.getAccountTime() == null ? null : DateUtil.formatDateTime(processDto.getAccountTime()));

            requestInfos.add(requestInfo);
        }

        com.xpay.common.statics.dto.account.AccountProcessResultDto resultDto = new com.xpay.common.statics.dto.account.AccountProcessResultDto();
        resultDto.setProcessResult(result.getProcessResult());
        resultDto.setErrCode(result.getErrCode());
        resultDto.setErrMsg(result.getErrMsg());
        resultDto.setRequestInfos(requestInfos);

        String topic = requestDto.getCallbackQueue();
        String tags = TopicGroup.ACCOUNT_MCH_GROUP;
        if(topic.indexOf(":") > 0){
            String[] topicArr = topic.split(":");
            topic = topicArr.length > 0 ? topicArr[0] : topic;
            tags = topicArr.length > 1 ? topicArr[1] : tags;
        }
        resultDto.setTopic(topic);
        resultDto.setTags(tags);
        return resultDto;
    }
}
