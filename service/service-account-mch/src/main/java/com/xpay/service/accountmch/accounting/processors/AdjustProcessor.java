package com.xpay.service.accountmch.accounting.processors;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.enums.account.AccountMchAmountTypeEnum;
import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.facade.accountmch.dto.AccountProcessDetailDto;
import com.xpay.service.accountmch.bo.AccountingBo;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @Description:调账：调整账户参数或者进行差错处理
 * @author: chenyf
 * @Date: 2019/9/5
 */
@Component
public class AdjustProcessor extends AccountingProcessor {
    private Logger logger = LoggerFactory.getLogger(CreditProcessor.class);

    /**
     * 账务处理的具体逻辑
     */
    @Override
    protected void doProcess(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo) {
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        if(processDto.getProcessType() == AccountProcessTypeEnum.ADJUST_ADD.getValue()){
            if(processDto.getAmountType() == AccountMchAmountTypeEnum.SETTLED_AMOUNT.getValue()){
                this.adjustSettledAdd(account, processDto, accountingBo);
            }else{
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
            }
        }else if(processDto.getProcessType() == AccountProcessTypeEnum.ADJUST_SUB.getValue()){
            if(processDto.getAmountType() == AccountMchAmountTypeEnum.SETTLED_AMOUNT.getValue()){
                this.adjustSettledSub(account, processDto, accountingBo);
            }else{
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
            }
        }else if(processDto.getProcessType() == AccountProcessTypeEnum.ADJUST_AMOUNT_RATIO.getValue()){
            if(processDto.getAmountType() == AccountMchAmountTypeEnum.TOTAL_ADVANCE_AMOUNT.getValue()){
                this.adjustAdvanceRatio(account, processDto, accountingBo);
            }else{
                throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
            }
        }else{
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的processType:"+processDto.getProcessType());
        }
    }

    /**
     * 差错处理 - 已结算调增
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private void adjustSettledAdd(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        AccountMch accountOld = copyAccount(account);
        account.setBalance(AmountUtil.add(account.getBalance(), processDto.getAmount()));
        account.setSettledAmount(AmountUtil.add(account.getSettledAmount(), processDto.getAmount()));

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }

    /**
     * 差错处理 - 已结算调减
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private void adjustSettledSub(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        if(AmountUtil.greater(processDto.getAmount(), account.getSettledAmount())){
            logger.error("processNo={} trxNo={} amount={} settledAmount={} 已结算金额不足", processDto.getProcessNo(),
                    processDto.getTrxNo(), processDto.getAmount(), account.getSettledAmount());
            throw new AccountMchBizException(AccountMchBizException.SETTLED_AMOUNT_NOT_ENOUGH, "已结算金额不足");
        }

        AccountMch accountOld = copyAccount(account);
        account.setBalance(AmountUtil.sub(account.getBalance(), processDto.getAmount()));
        account.setSettledAmount(AmountUtil.sub(account.getSettledAmount(), processDto.getAmount()));

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }

    /**
     * 调整垫资金额比例
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private void adjustAdvanceRatio(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        BigDecimal maxAdvanceAmount = processDto.getAmount();//使用此字段传递最大垫资金额
        BigDecimal advanceRatio = processDto.getFee();//使用此字段传递垫资比例
        if(AmountUtil.greater(advanceRatio, BigDecimal.ONE)){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "垫资比例不能超过100%");
        }else if(AmountUtil.lessThan(advanceRatio, BigDecimal.ZERO)){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "垫资比例不能低于0%");
        }

        processDto.setAmount(BigDecimal.ZERO);//设置为零，因为账务明细中不需要此值
        processDto.setFee(BigDecimal.ZERO);//设置为零，因为账务明细中不需要此值
        AccountMch accountOld = copyAccount(account);

        //设置新的值
        account.setMaxAvailAdvanceAmount(maxAdvanceAmount);
        account.setAdvanceRatio(advanceRatio);

        super.allocateAdvanceAmount(account);

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }
}
