package com.xpay.service.accountmch.accounting.processors;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.enums.account.AccountMchAmountTypeEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.facade.accountmch.dto.AccountMchDto;
import com.xpay.service.accountmch.bo.AccountingBo;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description: 账务入账逻辑
 * @author: chenyf
 * @Date: 2018/3/5
 */
@Component
public class CreditProcessor extends AccountingProcessor {
    private Logger logger = LoggerFactory.getLogger(CreditProcessor.class);

    /**
     * 账务处理的具体逻辑
     */
    @Override
    protected void doProcess(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        //入账不需要校验账户状态，出款时才需要

        if(processDto.getAmountType() == AccountMchAmountTypeEnum.TOTAL_ADVANCE_AMOUNT.getValue()){
            //入账到垫资金额(包含可垫+留存)
            this.advanceCredit(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.SETTLED_AMOUNT.getValue()){
            //入账到可结算金额(如：退汇)
            this.settleCredit(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.RSM_AMOUNT.getValue()){
            //风控冻结加款
            rsmCredit(account, processDto, accountingBo);
        }else{
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
        }
    }

    /**
     * 可结算金额入账
     */
    private void settleCredit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        AccountMch accountOld = copyAccount(account);
        account.setBalance(AmountUtil.add(account.getBalance(), processDto.getAmount()));
        account.setSettledAmount(AmountUtil.add(account.getSettledAmount(), processDto.getAmount()));

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }

    /**
     * 垫资金额入账
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private void advanceCredit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        //各金额字段入账
        AccountMch accountOld = copyAccount(account);
        account.setBalance(AmountUtil.add(account.getBalance(), processDto.getAmount()));
        account.setTotalAdvanceAmount(AmountUtil.add(account.getTotalAdvanceAmount(), processDto.getAmount()));
        account.setTotalCreditAmount(AmountUtil.add(account.getTotalCreditAmount(), processDto.getAmount()));

        super.allocateAdvanceAmount(account);
        super.addUpAdvanceOrderCount(account, processDto.getProcessType());

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }

    /**
     * 风控冻结
     * @param account
     * @param processDto
     */
    private void rsmCredit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        AccountMch accountOld = copyAccount(account);
        account.setRsmAmount(AmountUtil.add(account.getRsmAmount(), processDto.getAmount()));

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }
}
