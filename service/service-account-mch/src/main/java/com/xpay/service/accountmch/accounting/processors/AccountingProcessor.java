package com.xpay.service.accountmch.accounting.processors;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;
import com.xpay.common.statics.enums.account.AccountStatusEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.accountmch.dto.AdvanceDetailDto;
import com.xpay.facade.accountmch.dto.BussInfoDto;
import com.xpay.facade.accountmch.dto.ExtraDto;
import com.xpay.facade.accountmch.dto.OrderCountDto;
import com.xpay.service.accountmch.bo.AccountingBo;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.service.accountmch.entity.AccountProcessDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:账务处理逻辑的父类，账务处理选用策略模式
 * @author: chenyf
 * @Date: 2018/3/5
 */
public abstract class AccountingProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 执行账务处理的方法
     * @param account         账户记录
     * @param processDto      账务处理业务对象
     * @param accountingBo    记账业务对象，用以存储账务处理过程中的临时结果
     */
    public void process(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo){
        //1.校验账户状态
        this.validateAccountStatus(account, processDto.getProcessType());

        //2.执行账务处理
        this.doProcess(account, processDto, accountingBo);

        //3.校验账务处理结果的正确性
        this.validateAccountAmountCalcResult(processDto.getTrxNo(), account);
    }

    /**
     * 执行账务处理的方法
     * @param account         账务处理流水号
     * @param processDto      账务处理业务对象
     * @param accountingBo    账户记录
     */
    protected abstract void doProcess(AccountMch account, AccountProcessDto processDto, AccountingBo accountingBo);

    /**
     * 复制账户对象
     * @param account
     * @return
     */
    public AccountMch copyAccount(AccountMch account){
        AccountMch copy = BeanUtil.newAndCopy(account, AccountMch.class);
        return copy;
    }

    /**
     * 创建账务明细对象
     * @param accountOld
     * @param account
     * @param processDto
     * @return
     */
    public static AccountProcessDetail buildAccountProcessDetail(AccountMch accountOld, AccountMch account, AccountProcessDto processDto){
        AdvanceDetailDto advanceDetail = new AdvanceDetailDto();
        advanceDetail.setAdvanceRatio(account.getAdvanceRatio());
        advanceDetail.setMaxAvailAdvanceAmount(account.getMaxAvailAdvanceAmount());
        advanceDetail.setTotalCreditAmount(account.getTotalCreditAmount());
        advanceDetail.setTotalDebitAmount(account.getTotalDebitAmount());
        advanceDetail.setTotalReturnAmount(account.getTotalReturnAmount());
        advanceDetail.setTotalCrossReturnAmount(account.getTotalCrossReturnAmount());
        advanceDetail.setTotalAdvanceDebitAmount(account.getTotalAdvanceDebitAmount());
        advanceDetail.setTotalAdvanceReturnAmount(account.getTotalAdvanceReturnAmount());
        advanceDetail.setTotalOrderCount(account.getTotalOrderCount());

        BussInfoDto bussDto = new BussInfoDto();
        bussDto.setBussType(processDto.getBussType());
        bussDto.setBussCode(processDto.getBussCode());

        ExtraDto extraDto = new ExtraDto();
        extraDto.setBussInfo(bussDto);
        extraDto.setAdvanceDetail(advanceDetail);

        AccountProcessDetail detail = new AccountProcessDetail();
        detail.setVersion(0);
        detail.setCreateTime(processDto.getAccountTime() == null ?  new Date() : processDto.getAccountTime());
        detail.setCreateDate(detail.getCreateTime());
        detail.setAccountNo(account.getAccountNo());

        detail.setBalance(account.getBalance());
        detail.setSettledAmount(account.getSettledAmount());
        detail.setUnsettleAmount(account.getUnsettleAmount());
        detail.setRsmAmount(account.getRsmAmount());
        detail.setTotalAdvanceAmount(account.getTotalAdvanceAmount());
        detail.setAvailAdvanceAmount(account.getAvailAdvanceAmount());
        detail.setRetainAdvanceAmount(account.getRetainAdvanceAmount());

        detail.setAlterBalance(AmountUtil.sub(account.getBalance(), accountOld.getBalance()));
        detail.setAlterSettledAmount(AmountUtil.sub(account.getSettledAmount(), accountOld.getSettledAmount()));
        detail.setAlterTotalAdvanceAmount(AmountUtil.sub(account.getTotalAdvanceAmount(), accountOld.getTotalAdvanceAmount()));
        detail.setAlterAdvanceAmount(AmountUtil.sub(account.getAvailAdvanceAmount(), accountOld.getAvailAdvanceAmount()));
        detail.setAlterRetainAmount(AmountUtil.sub(account.getRetainAdvanceAmount(), accountOld.getRetainAdvanceAmount()));

        detail.setProcessNo(processDto.getProcessNo());
        detail.setRequestNo(processDto.getTrxNo());
        detail.setMchRequestNo(processDto.getMchTrxNo());
        detail.setTrxTime(processDto.getTrxTime());
        detail.setTrxAmount(processDto.getAmount());
        detail.setFee(processDto.getFee());
        detail.setRemark(processDto.getDesc());
        detail.setProcessType(processDto.getProcessType());
        detail.setLiquidateVersion(account.getLiquidateVersion());
        detail.setExtraInfo(JsonUtil.toJson(extraDto));
        //总垫资金额有变动则需要参与清结算，无变动则不需要参与清结算
        if(AmountUtil.equal(account.getTotalAdvanceAmount(), accountOld.getTotalAdvanceAmount())){
            detail.setLiquidation(PublicStatus.INACTIVE);
        }else{
            detail.setLiquidation(PublicStatus.ACTIVE);
        }
        return detail;
    }

    /**
     * 校验账户状态
     * @param account
     * @param processType
     */
    protected void validateAccountStatus(AccountMch account, Integer processType){
        if(account.getStatus().intValue() == AccountStatusEnum.CANCELLED.getValue()){
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_STATUS_IS_INACTIVE, "账户已注销");
        }else if(processType == AccountProcessTypeEnum.CREDIT.getValue()){
            if (account.getStatus().intValue() == AccountStatusEnum.FREEZING.getValue()) {
                throw new AccountMchBizException(AccountMchBizException.ACCOUNT_STATUS_IS_INACTIVE, "账户冻结");
            }else if(account.getStatus().intValue() == AccountStatusEnum.FREEZE_CREDIT.getValue()){
                throw new AccountMchBizException(AccountMchBizException.ACCOUNT_STATUS_IS_INACTIVE, "账户止收");
            }
        }else if(processType == AccountProcessTypeEnum.DEBIT_OUT.getValue()){
            if (account.getStatus().intValue() == AccountStatusEnum.FREEZING.getValue()) {
                throw new AccountMchBizException(AccountMchBizException.ACCOUNT_STATUS_IS_INACTIVE, "账户冻结");
            }else if(account.getStatus().intValue() == AccountStatusEnum.FREEZE_DEBIT.getValue()){
                throw new AccountMchBizException(AccountMchBizException.ACCOUNT_STATUS_IS_INACTIVE, "账户止付");
            }
        }
    }

    /**
     * 分配垫资额度，即把总垫资金额划分成可垫资金额、留存金额
     * @param account 账户对象
     */
    protected void allocateAdvanceAmount(AccountMch account){
        BigDecimal totalIncome = AmountUtil.add(account.getTotalCreditAmount(), account.getTotalCrossReturnAmount());//总收入 = 当日总收单 + 隔日退回
        BigDecimal currentAdvanceAmount = AmountUtil.mul(totalIncome, account.getAdvanceRatio()); //当前垫资额度 = 总收入 * 垫资比例
        if(AmountUtil.greater(currentAdvanceAmount, account.getMaxAvailAdvanceAmount())){ //当前垫资额度 不能超过 最大垫资额度
            currentAdvanceAmount = account.getMaxAvailAdvanceAmount();
        }

        BigDecimal successDebitAdvanceAmount = AmountUtil.sub(account.getTotalAdvanceDebitAmount(), account.getTotalAdvanceReturnAmount());//已成功出款的可用垫资总额 = 可用垫资出款总额 - 可用垫资退回总额
        BigDecimal availAdvanceAmount = AmountUtil.sub(currentAdvanceAmount, successDebitAdvanceAmount);//还能用来进行垫资的金额 = 当前垫资额度 - 已成功出款的可垫资总额
        if(AmountUtil.lessThanOrEqualTo(availAdvanceAmount, BigDecimal.ZERO)){
            //此种情况说明 已成功出款的可垫资总额 已经超过 当前垫资额度，也就是没有任何金额可以分配给 可用垫资金额 了
            //出现这种用超的情况，有可能是把最大垫资额度或者垫资比例调小了
            availAdvanceAmount = BigDecimal.ZERO;
        }

        account.setCurrentAdvanceAmount(currentAdvanceAmount);
        if(AmountUtil.greater(availAdvanceAmount, account.getTotalAdvanceAmount())){
            //出现这种情况，有可能是把最大垫资额度或者垫资比例调大了，并且在调整之前，留存金额已经被出掉了一些
            account.setAvailAdvanceAmount(account.getTotalAdvanceAmount());
        }else{
            account.setAvailAdvanceAmount(availAdvanceAmount);
        }
        account.setRetainAdvanceAmount(AmountUtil.sub(account.getTotalAdvanceAmount(), account.getAvailAdvanceAmount()));
    }

    /**
     * 累加垫资订单笔数，注意：非垫资账户的账务处理不要调用此方法进行累加
     * @param account
     * @param processType
     */
    protected void addUpAdvanceOrderCount(AccountMch account, Integer processType){
        OrderCountDto countDto = OrderCountDto.fromAccountOrderCount(account.getTotalOrderCount());
        if(processType == AccountProcessTypeEnum.CREDIT.getValue()){
            countDto.setTotalReceiveCount(countDto.getTotalReceiveCount() + 1);
        }else if(processType == AccountProcessTypeEnum.DEBIT_OUT.getValue()){
            countDto.setTotalDebitCount(countDto.getTotalDebitCount() + 1);
        }else if(processType == AccountProcessTypeEnum.DEBIT_RETURN.getValue()){
            countDto.setTotalReturnCount(countDto.getTotalReturnCount() + 1);
        }
        account.setTotalOrderCount(countDto.getTotalReceiveCount()+","+countDto.getTotalDebitCount()+","+countDto.getTotalReturnCount());
    }

    /**
     * 把账务处理结果保存到 accountingBo 里面
     * @param accountMch
     * @param processDetail
     * @param accountingBo
     */
    protected void holdAccountingResult(AccountMch accountMch, AccountProcessDetail processDetail, AccountingBo accountingBo){
        List<AccountProcessDetail> detailList = accountingBo.getAccountMapDetail().get(accountMch);
        if(detailList != null){
            detailList.add(processDetail);
        }else{
            detailList = new ArrayList<>();
            detailList.add(processDetail);
            accountingBo.getAccountMapDetail().put(accountMch, detailList);
        }
    }

    /**
     * 校验账务处理完毕之后各金额字段是否异常（各金额不能小于0的已通过设置数据库的字段为unsigned来处理）
     * @param account
     */
    protected void validateAccountAmountCalcResult(String trxNo, AccountMch account) {
        if(AmountUtil.lessThan(account.getBalance(), BigDecimal.ZERO)) {
            //此校验可保证用掉的金额不会超过其总账户余额
            logger.error("trxNo={} accountNo={} balance={} 账户余额小于零", trxNo, account.getAccountNo(), account.getBalance());
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "账户余额小于零");
        }else if(! AmountUtil.equal(account.getBalance(), AmountUtil.add(account.getUnsettleAmount(), account.getSettledAmount(), account.getTotalAdvanceAmount()))) {
            //此校验确保 总余额 = 余额账户 + 垫资账户
            logger.error("trxNo={} accountNo={} balance={} unsettleAmount={} settledAmount={} totalAdvanceAmount={}  账户总余额不等于各个金额总和",
                    trxNo, account.getAccountNo(), account.getBalance(), account.getUnsettleAmount(), account.getSettledAmount(), account.getTotalAdvanceAmount());
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "账户总余额不等于各个金额总和");
        }else if(! AmountUtil.equal(getTotalAdvanceAmountFromBalance(account), account.getTotalAdvanceAmount())){
            //此校验确保 垫资账户计算出来的垫资总额与总账户余额计算出来的垫资总额是相等的
            logger.error("trxNo={} accountNo={} totalAdvanceAmountFromBalance={} totalAdvanceAmount={} 总账户余额算出的垫资总额与垫资账户的垫资总额不相等",
                    trxNo, account.getAccountNo(), getTotalAdvanceAmountFromBalance(account), account.getTotalAdvanceAmount());
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "总余额-垫资计算结果不相等");
        }else if(! AmountUtil.equal(account.getTotalAdvanceAmount(), getTotalAdvanceAmountFromAdvanceField(account))){
            //此校验确保 总垫资 与 各个垫资金额总和 不会有计算错误
            logger.error("trxNo={} accountNo={} totalAdvanceAmount={} totalAdvanceAmountFromAdvanceField={} 垫资总额与各垫资金额总和不相等",
                    trxNo, account.getAccountNo(), account.getTotalAdvanceAmount(), getTotalAdvanceAmountFromAdvanceField(account));
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "总垫资-垫资金额总和计算结果不相等");
        }else if(! AmountUtil.equal(account.getTotalAdvanceAmount(), getTotalAdvanceAmountFromBudget(account))){
            //此校验确保 总垫资 与 垫资总收支 不会计算错误
            logger.error("trxNo={} accountNo={} totalAdvanceAmount={} totalAdvanceAmountFromBudget={} 垫资总额与垫资收支算出的垫资总额不相等",
                    trxNo, account.getAccountNo(), account.getTotalAdvanceAmount(), getTotalAdvanceAmountFromBudget(account));
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "总垫资-收支总垫资计算结果不相等");
        }
    }

    /**
     * 校验垫资出款时 可用垫资金额、留存垫资金额 计算结果是否正确
     * @param trxNo
     * @param expectAdvance
     * @param realAdvanceAmount
     * @param expectRetain
     * @param realRetain
     */
    protected void validateAdvanceAmountDebitResult(String trxNo, BigDecimal expectAdvance, BigDecimal realAdvanceAmount,
                                                        BigDecimal expectRetain, BigDecimal realRetain) {
        if(! AmountUtil.equal(expectAdvance, realAdvanceAmount.abs())){
            logger.error("trxNo={} expectAdvance={} realAdvanceAmount={} 预期可垫出款金额与实际可垫出款金额不相等",
                    trxNo, expectAdvance, realAdvanceAmount.abs());
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "预期可垫出款金额与实际可垫出款金额不相等");
        }else if(! AmountUtil.equal(expectRetain, realRetain.abs())){
            logger.error("trxNo={} expectRetain={} realRetain={} 预期留存出款金额与实际留存出款金额不相等",
                    trxNo, expectRetain, realRetain.abs());
            throw new AccountMchBizException(AccountMchBizException.ACCOUNT_AMOUNT_CALC_ERROR, "预期留存出款金额与实际留存出款金额不相等");
        }
    }

    /**-----------------------------------------------计算总垫资金额的三种方式，正常情况下三种计算方式得出的结果是一样的----------------------------------------**/
    /**
     * 从余额角度计算 总垫资金额 = 总余额 - 已结算金额 - 待结算金额
     * @return
     */
    public BigDecimal getTotalAdvanceAmountFromBalance(AccountMch account){
        return AmountUtil.sub(account.getBalance(), account.getSettledAmount(), account.getUnsettleAmount());
    }
    /**
     * 从总收入、总支出角度计算 总垫资金额 = 当日累计收 + 当日累计退回 - 当日累计冻结 = 可垫 + 留存
     * @return
     */
    public BigDecimal getTotalAdvanceAmountFromBudget(AccountMch account){
        return AmountUtil.sub(AmountUtil.add(account.getTotalCreditAmount(), account.getTotalReturnAmount()), account.getTotalDebitAmount());
    }
    /**
     * 直接从字段计算 总垫资金额 = 可垫 + 留存 = 当日累计收 + 当日累计退回 - 当日累计冻结
     * @return
     */
    public BigDecimal getTotalAdvanceAmountFromAdvanceField(AccountMch account){
        return AmountUtil.add(account.getAvailAdvanceAmount(), account.getRetainAdvanceAmount());
    }
    /**-----------------------------------------------计算总垫资金额的三种方式，正常情况下三种计算方式得出的结果是一样的----------------------------------------**/


    /**
     * 可用余额 = 已结算金额 + 总垫资金额
     * @return
     */
    public BigDecimal getAvailBalanceAmount(AccountMch account){
        return AmountUtil.add(account.getSettledAmount(), account.getTotalAdvanceAmount());
    }
    /**
     * 可出款金额 = 可结 + 可垫
     * @return
     */
    public BigDecimal getDebitAbleAmount(AccountMch account){
        return AmountUtil.add(account.getSettledAmount(), account.getAvailAdvanceAmount());
    }
}
