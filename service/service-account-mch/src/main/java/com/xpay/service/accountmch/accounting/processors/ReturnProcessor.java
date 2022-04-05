package com.xpay.service.accountmch.accounting.processors;

import com.xpay.common.statics.dto.account.AccountDetailDto;
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
 * @Description:账务退回逻辑
 * @author: chenyf
 * @Date: 2019/9/5
 */
@Component
public class ReturnProcessor extends AccountingProcessor {
    private Logger logger = LoggerFactory.getLogger(ReturnProcessor.class);

    /**
     * 账务处理的具体逻辑
     */
    @Override
    protected void doProcess(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        if(processDto.getAmountType() == AccountMchAmountTypeEnum.SOURCE_DEBIT_AMOUNT.getValue()){
            //原路退回
            this.sourceReturn(account, processDto, accountingBo);
        }else if(processDto.getAmountType() == AccountMchAmountTypeEnum.RSM_AMOUNT.getValue()){
            //风控解冻退回
            this.rsmReturn(account, processDto, accountingBo);
        }else{
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
        }
    }

    /**
     * 原路退回
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private void sourceReturn(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        AccountDetailDto detailDto = processDto.getAccountDetailDto();
        if(detailDto == null){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "原出款记录不能为null");
        }else if(account.getLiquidateVersion() < detailDto.getLiquidateVersion()){ //正常情况下，账户表上的清算版本号一定是大于等于账务明细上的清算版本号的
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "清算版本号不正确！");
        }else if(! AmountUtil.equal(processDto.getAmount(), detailDto.getAlterAmount())){
            //由于出款金额可能用到多个金额，如果支持部分退回，将不好计算每个金额字段应退回多少，所以，此处限定只能全额退回，如果希望做到只退回部分金额
            //可以在做退回的时候同时传入两个AccountProcessDto，第一个Dto全额退回，第二个Dto再扣除一部分金额
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "退回金额不等于出款时的扣款金额");
        }

        AccountMch accountOld = copyAccount(account);

        //设置各个字段的值为绝对值，为了方便接下来的计算
        detailDto.setAlterSettleAmount(detailDto.getAlterSettleAmount().abs());
        detailDto.setAlterAdvanceAmount(detailDto.getAlterAdvanceAmount().abs());
        detailDto.setAlterRetainAmount(detailDto.getAlterRetainAmount().abs());
        BigDecimal usedAllAdvanceAmount = AmountUtil.add(detailDto.getAlterAdvanceAmount(), detailDto.getAlterRetainAmount());

        //处理各个金额字段的值
        account.setBalance(AmountUtil.add(account.getBalance(), detailDto.getAlterAmount()));
        account.setSettledAmount(AmountUtil.add(account.getSettledAmount(), detailDto.getAlterSettleAmount()));

        if(AmountUtil.greater(usedAllAdvanceAmount, BigDecimal.ZERO)) {
            account.setTotalAdvanceAmount(AmountUtil.add(account.getTotalAdvanceAmount(), usedAllAdvanceAmount));
            account.setTotalReturnAmount(AmountUtil.add(account.getTotalReturnAmount(), usedAllAdvanceAmount));

            if(account.getLiquidateVersion().equals(detailDto.getLiquidateVersion())){
                account.setTotalAdvanceReturnAmount(AmountUtil.add(account.getTotalAdvanceReturnAmount(), detailDto.getAlterAdvanceAmount()));
            }else{
                //跨清算日退回的垫资，应该入账到跨日退回金额中
                account.setTotalCrossReturnAmount(AmountUtil.add(account.getTotalCrossReturnAmount(), usedAllAdvanceAmount));
            }

            super.allocateAdvanceAmount(account);
            super.addUpAdvanceOrderCount(account, processDto.getProcessType());
        }

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }

    /**
     * 风控解冻退回
     */
    private void rsmReturn(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        if(AmountUtil.greater(processDto.getAmount(), account.getRsmAmount())){
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "风控冻结金额不足");
        }

        AccountMch accountOld = copyAccount(account);
        account.setRsmAmount(AmountUtil.sub(account.getRsmAmount(), processDto.getAmount()));

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }
}
