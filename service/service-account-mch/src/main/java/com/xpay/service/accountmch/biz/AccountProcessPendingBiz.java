package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.account.AccountProcessPendingStageEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.facade.accountmch.dto.PendingInfoDto;
import com.xpay.facade.accountmch.dto.AccountProcessPendingDto;
import com.xpay.service.accountmch.dao.AccountProcessPendingDao;
import com.xpay.service.accountmch.dao.AccountProcessPendingHistoryDao;
import com.xpay.service.accountmch.entity.AccountProcessPending;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description:待账务处理逻辑处理层
 * @author: chenyf
 * @Date: 2018/3/8
 */
@Component
public class AccountProcessPendingBiz {
    @Autowired
    private AccountProcessPendingDao accountProcessPendingDao;
    @Autowired
    private AccountProcessPendingHistoryDao accountProcessPendingHistoryDao;

    public void add(AccountProcessPending accountProcessPending){
        accountProcessPendingDao.insert(accountProcessPending);
    }

    /**
     * 把待处理记录的处理阶段从待处理更新为处理中
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePendingStatus(Long id, AccountProcessPendingStageEnum stageNew, AccountProcessPendingStageEnum stageOld){
        boolean isSuccess = accountProcessPendingDao.updatePendingStatus(id, stageNew, stageOld);
        if(! isSuccess){
            //如果更新失败，则需要抛出异常，避免同一笔单多次处理
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "待账务处理记录更新状态失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePendingStatus(List<Long> idList, AccountProcessPendingStageEnum stageNew, AccountProcessPendingStageEnum stageOld){
        boolean isSuccess = accountProcessPendingDao.updatePendingStatus(idList, stageNew, stageOld);
        if(! isSuccess){
            //如果更新失败，则需要抛出异常，避免同一笔单多次处理
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "待账务处理记录更新状态失败");
        }
    }

    /**
     * 把待账务处理记录从处理中审核为待处理，
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean auditProcessPendingFromProcessingToPending(Long id){
        int waitMinute = 3;
        AccountProcessPending pending = accountProcessPendingDao.getById(id);
        if(pending == null){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "待账务处理记录不存在");
        }else if(System.currentTimeMillis() - pending.getModifyTime().getTime() < waitMinute * 60 * 1000L){ //避免系统正在处理，又被人工审核为'待处理'
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "还不能审核此待账务处理记录，请"+waitMinute+"分钟后再试");
        }else if(pending.getProcessStage().intValue() == AccountProcessPendingStageEnum.PENDING.getValue()){
            return true;
        }
       updatePendingStatus(id, AccountProcessPendingStageEnum.PENDING, AccountProcessPendingStageEnum.PROCESSING);
        return true;
    }

    public PageResult<List<AccountProcessPendingDto>> listByPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<AccountProcessPending>> result = accountProcessPendingDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AccountProcessPendingDto.class), result);
    }

    public PageResult<List<AccountProcessPendingDto>> listHistoryByPage(Map<String, Object> paramMap, PageQuery pageQuery){
        if(paramMap == null || paramMap.get("createTimeBegin") == null){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "查询历史数据需要传入开始时间参数beginTime");
        }

        PageResult<List<AccountProcessPending>> result = accountProcessPendingHistoryDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AccountProcessPendingDto.class), result);
    }

    public List<AccountProcessPendingDto> listByPendingByIdList(List<Long> idList){
        if(idList == null || idList.isEmpty()){
            return null;
        }
        List<AccountProcessPending> pendingList = accountProcessPendingDao.listByIdList(idList);
        return BeanUtil.newAndCopy(pendingList, AccountProcessPendingDto.class);
    }

    public AccountProcessPendingDto getAccountProcessPendingById(Long id){
        if(id == null){
            return null;
        }

        AccountProcessPending pending = accountProcessPendingDao.getById(id);
        if(pending != null){
            return BeanUtil.newAndCopy(pending, AccountProcessPendingDto.class);
        }

        pending = accountProcessPendingHistoryDao.getById(id);
        return BeanUtil.newAndCopy(pending, AccountProcessPendingDto.class);
    }
    
    public List<Long> listUnMergeAccountProcessPendingId(List<Date> createDates, int status, int offset, int limit){
        if(createDates== null || createDates.isEmpty() || offset < 0 || limit <= 0){
            return null;
        }

        List<String> createDateStrList = new ArrayList<>(createDates.size());
        createDates.forEach(date -> createDateStrList.add(DateUtil.formatDate(date)));

    	Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("createDateList", createDateStrList);
    	paramMap.put("processStage", status);
        paramMap.put("mergeSupport", PublicStatus.INACTIVE);
    	return accountProcessPendingDao.listAccountProcessPendingId(paramMap, offset, limit);
    }

    public List<PendingInfoDto> listMergeAbleAccountProcessPendingInfo(List<Date> createDates, int status, int offset, int limit){
        if(createDates== null || createDates.isEmpty() || offset < 0 || limit <= 0){
            return null;
        }

        List<String> createDateStrList = new ArrayList<>(createDates.size());
        createDates.forEach(date -> createDateStrList.add(DateUtil.formatDate(date)));

        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("createDateList", createDateStrList);
        paramMap.put("processStage", status);
        paramMap.put("mergeSupport", PublicStatus.ACTIVE);
        return accountProcessPendingDao.listAccountProcessPendingInfo(paramMap, offset, limit);
    }

    public int countProcessingTooLongRecord(List<Date> createDates, Integer processStage, Integer timeDiffSecond){
        if(createDates== null || createDates.isEmpty() || processStage == null || timeDiffSecond == null || timeDiffSecond <= 0){
            return -1;
        }
        return accountProcessPendingDao.countProcessingTooLongRecord(createDates, processStage, timeDiffSecond);
    }
}
