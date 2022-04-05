package com.xpay.service.accountmch.accounting.processors;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.enums.account.AccountMchAmountTypeEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.service.accountmch.bo.AccountingBo;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @Description:账务出款逻辑
 * @author: chenyf
 * @Date: 2019/9/5
 */
@Component
public class DebitProcessor extends AccountingProcessor {
    private Logger logger = LoggerFactory.getLogger(DebitProcessor.class);

    /**
     * 账务处理的具体逻辑
     */
    @Override
    protected void doProcess(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        if(AmountUtil.lessThan(account.getBalance(), processDto.getAmount())){ //总余额不足(可用余额 = 已结+可垫+留存)
            throw new AccountMchBizException(AccountMchBizException.TOTAL_BALANCE_NOT_ENOUGH, "总余额不足");
        }else if(AmountUtil.lessThan(getAvailBalanceAmount(account), processDto.getAmount())){ //可用金额不足
            throw new AccountMchBizException(AccountMchBizException.AVAIL_BALANCE_NOT_ENOUGH, "可用余额不足");
        }else if(AmountUtil.lessThanOrEqualTo(account.getBalance(), account.getRsmAmount())){ //风控已超额冻结
            throw new AccountMchBizException(AccountMchBizException.AVAIL_BALANCE_NOT_ENOUGH_FOR_RCMS, "可用余额不足-风控超额冻结");
        }else if(AmountUtil.lessThan(AmountUtil.sub(account.getBalance(), account.getRsmAmount()), processDto.getAmount())){//扣除掉风控冻结金额之后已不足出款
            //这种校验方式能确保总账户上有一笔钱可以被控住而不会被出掉，但是做不到指定结算账户还是垫资账户上的钱不被出掉，这个需要看业务是否能够接受，
            // 如果不能接受，可能需要拆分成多个字段值，实现方案会比较复杂
            throw new AccountMchBizException(AccountMchBizException.AVAIL_BALANCE_NOT_ENOUGH_FOR_RCMS, "可用余额不足-风控冻结");
        }

        if(processDto.getAmountType() == AccountMchAmountTypeEnum.SETTLED_AMOUNT.getValue()){
            //使用 已结算金额 扣款
            this.settleDebit(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.AVAIL_ADVANCE_AMOUNT.getValue()){
            //使用 可用垫资金额 扣款
            this.availAdvanceDebit(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.SETTLED_OR_AVAIL_ADVANCE_AMOUNT.getValue()){
            //使用 已结算 或 可用垫资金额 扣款
            this.settleOrAvailAdvanceDebit(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.SETTLED_AND_AVAIL_ADVANCE_AMOUNT.getValue()){
            //使用 已结算 + 可用垫资金额 扣款
            this.settleAndAvailAdvanceDebit(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.AVAIL_BALANCE_AMOUNT.getValue()){
            //使用 可用余额(已结+可垫+留存) 扣款
            this.availBalanceDebit(account, processDto, accountingBo);
        }else{
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
        }
    }

    /**
     * 使用已结算出款
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private AccountProcessDetail settleDebit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
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
        return accountDetail;
    }

    /**
     * 使用已结算或可垫资出款
     * @param account
     * @param processDto
     * @param accountingBo
     * @return
     */
    private AccountProcessDetail settleOrAvailAdvanceDebit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        if(AmountUtil.greaterThanOrEqualTo(account.getSettledAmount(), processDto.getAmount())){
            return settleDebit(account, processDto, accountingBo);
        }else{
            return availAdvanceDebit(account, processDto, accountingBo);
        }
    }

    /**
     * 使用可用垫资总结
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private AccountProcessDetail availAdvanceDebit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        if(AmountUtil.greater(processDto.getAmount(), account.getAvailAdvanceAmount())){
            logger.error("processNo={} trxNo={} amount={} availAdvanceAmount={} 可用垫资额度不足", processDto.getProcessNo(),
                    processDto.getTrxNo(), processDto.getAmount(), account.getAvailAdvanceAmount());
            throw new AccountMchBizException(AccountMchBizException.AVAIL_ADVANCE_AMOUNT_NOT_ENOUGH, "可用垫资额度不足");
        }

        AccountMch accountOld = copyAccount(account);
        account.setBalance(AmountUtil.sub(account.getBalance(), processDto.getAmount()));
        account.setTotalAdvanceAmount(AmountUtil.sub(account.getTotalAdvanceAmount(), processDto.getAmount()));
        account.setTotalDebitAmount(AmountUtil.add(account.getTotalDebitAmount(), processDto.getAmount()));
        account.setTotalAdvanceDebitAmount(AmountUtil.add(account.getTotalAdvanceDebitAmount(), processDto.getAmount()));

        super.allocateAdvanceAmount(account);
        super.addUpAdvanceOrderCount(account, processDto.getProcessType());

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);

        //出款金额校验
        validateAdvanceAmountDebitResult(processDto.getTrxNo(), processDto.getAmount(),
                accountDetail.getAlterAdvanceAmount(), BigDecimal.ZERO, accountDetail.getAlterRetainAmount());

        holdAccountingResult(account, accountDetail, accountingBo);
        return accountDetail;
    }

    /**
     * 使用已结算+可用垫资出款
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private AccountProcessDetail settleAndAvailAdvanceDebit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        BigDecimal debitAbleAmount = getDebitAbleAmount(account);
        if(AmountUtil.greater(processDto.getAmount(), debitAbleAmount)){
            logger.error("processNo={} trxNo={} amount={} debitAbleAmount={} 可出款金额不足", processDto.getProcessNo(),
                    processDto.getTrxNo(), processDto.getAmount(), debitAbleAmount);
            throw new AccountMchBizException(AccountMchBizException.DEBIT_ABLE_AMOUNT_NOT_ENOUGH, "可出款金额不足");
        }

        if(AmountUtil.greaterThanOrEqualTo(account.getSettledAmount(), processDto.getAmount())){ //优先使用已结算金额
            return settleDebit(account, processDto, accountingBo);
        }

        AccountMch accountOld = copyAccount(account);

        BigDecimal usedAvailAdvanceAmount = AmountUtil.sub(processDto.getAmount(), account.getSettledAmount());
        account.setBalance(AmountUtil.sub(account.getBalance(), processDto.getAmount()));
        account.setSettledAmount(BigDecimal.ZERO);
        account.setTotalAdvanceAmount(AmountUtil.sub(account.getTotalAdvanceAmount(), usedAvailAdvanceAmount));
        account.setTotalDebitAmount(AmountUtil.add(account.getTotalDebitAmount(), usedAvailAdvanceAmount));
        account.setTotalAdvanceDebitAmount(AmountUtil.add(account.getTotalAdvanceDebitAmount(), usedAvailAdvanceAmount));

        super.allocateAdvanceAmount(account);
        super.addUpAdvanceOrderCount(account, processDto.getProcessType());

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);

        //出款金额校验
        validateAdvanceAmountDebitResult(processDto.getTrxNo(), usedAvailAdvanceAmount,
                accountDetail.getAlterAdvanceAmount(), BigDecimal.ZERO, accountDetail.getAlterRetainAmount());

        holdAccountingResult(account, accountDetail, accountingBo);
        return accountDetail;
    }

    /**
     * 使用可用余额出款(即可结算+可垫资+留存)，在退款等场景使用
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private AccountProcessDetail availBalanceDebit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        BigDecimal availBalance = getAvailBalanceAmount(account);
        if(AmountUtil.greater(processDto.getAmount(), availBalance)){
            logger.error("processNo={} trxNo={} amount={} availBalance={} 可用余额不足", processDto.getProcessNo(),
                    processDto.getTrxNo(), processDto.getAmount(), availBalance);
            throw new AccountMchBizException(AccountMchBizException.AVAIL_BALANCE_NOT_ENOUGH, "可用余额不足");
        }

        if(AmountUtil.greaterThanOrEqualTo(account.getSettledAmount(), processDto.getAmount())){ //第一优先使用已结算金额
            return this.settleDebit(account, processDto, accountingBo);
        }else if(AmountUtil.greaterThanOrEqualTo(getDebitAbleAmount(account), processDto.getAmount())){ //第二优先使用 已结算 + 可垫 金额
            return this.settleAndAvailAdvanceDebit(account, processDto, accountingBo);
        }

        //第三优先使用 已结算 + 可垫 + 留存 金额
        AccountMch accountOld = copyAccount(account);
        BigDecimal usedTotalAdvanceAmount = AmountUtil.sub(processDto.getAmount(), account.getSettledAmount()); //使用到的总垫资金额
        BigDecimal usedAvailAdvanceAmount = account.getAvailAdvanceAmount(); //使用到的可用垫资金额
        BigDecimal usedRetainAmount = AmountUtil.sub(usedTotalAdvanceAmount, usedAvailAdvanceAmount);

        account.setBalance(AmountUtil.sub(account.getBalance(), processDto.getAmount()));
        account.setSettledAmount(BigDecimal.ZERO);
        account.setTotalAdvanceAmount(AmountUtil.sub(account.getTotalAdvanceAmount(), usedTotalAdvanceAmount));
        account.setTotalDebitAmount(AmountUtil.add(account.getTotalDebitAmount(), usedTotalAdvanceAmount));
        account.setTotalAdvanceDebitAmount(AmountUtil.add(account.getTotalAdvanceDebitAmount(), usedAvailAdvanceAmount));

        super.allocateAdvanceAmount(account);
        super.addUpAdvanceOrderCount(account, processDto.getProcessType());

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);

        //出款金额校验
        validateAdvanceAmountDebitResult(processDto.getTrxNo(), usedAvailAdvanceAmount,
                accountDetail.getAlterAdvanceAmount(), usedRetainAmount, accountDetail.getAlterRetainAmount());

        holdAccountingResult(account, accountDetail, accountingBo);
        return accountDetail;
    }
}
