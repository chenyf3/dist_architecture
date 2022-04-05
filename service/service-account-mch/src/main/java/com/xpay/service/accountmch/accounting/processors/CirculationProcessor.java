package com.xpay.service.accountmch.accounting.processors;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.enums.account.AccountMchAmountTypeEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.facade.accountmch.dto.AccountMchDto;
import com.xpay.facade.accountmch.dto.AccountProcessDetailDto;
import com.xpay.service.accountmch.bo.AccountingBo;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description:账户内资金流转
 * @author: chenyf
 * @Date: 2019/9/5
 */
@Component
public class CirculationProcessor extends AccountingProcessor {
    private Logger logger = LoggerFactory.getLogger(CreditProcessor.class);

    /**
     * 账务处理的具体逻辑
     */
    @Override
    protected void doProcess(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo) {
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());

        if(processDto.getAmountType() == AccountMchAmountTypeEnum.UNSETTLE_TO_SETTLED_AMOUNT.getValue()){
            //待清算转移到已结算入账
            this.unSettleToSettleCredit(account, processDto, accountingBo);
        }else{
            throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "未预期的amountType:"+processDto.getAmountType());
        }
    }

    /**
     * 待结算金额转移到可结算金额上面
     * @param account
     * @param processDto
     * @param accountingBo
     */
    private void unSettleToSettleCredit(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo) {
        logger.info("processNo={} accountNo={} trxNo={}", processDto.getProcessNo(), account.getAccountNo(), processDto.getTrxNo());
        if(AmountUtil.greater(processDto.getAmount(), account.getUnsettleAmount())){
            logger.info("processNo={} trxNo={} amount={} unsettleAmount={} 待结算金额不足", processDto.getProcessNo(),
                    processDto.getTrxNo(), processDto.getAmount().toString(), account.getUnsettleAmount().toString());
            throw new AccountMchBizException(AccountMchBizException.UNSETTLE_AMOUNT_NOT_ENOUGH, "待结算金额不足");
        }

        AccountMch accountOld = copyAccount(account);
        account.setSettledAmount(AmountUtil.add(account.getSettledAmount(), processDto.getAmount()));
        account.setUnsettleAmount(AmountUtil.sub(account.getUnsettleAmount(), processDto.getAmount()));

        AccountProcessDetail accountDetail = buildAccountProcessDetail(accountOld, account, processDto);
        holdAccountingResult(account, accountDetail, accountingBo);
    }
}
