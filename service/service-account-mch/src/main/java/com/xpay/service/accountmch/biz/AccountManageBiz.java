package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.constants.common.DistLockConst;
import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.enums.account.AccountMchAmountTypeEnum;
import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;
import com.xpay.common.statics.enums.account.AccountStatusEnum;
import com.xpay.common.statics.enums.product.ProductCodeEnum;
import com.xpay.common.statics.enums.product.ProductTypeEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.accountmch.config.AccountingConfig;
import com.xpay.service.accountmch.dao.AccountMchDao;
import com.xpay.service.accountmch.entity.AccountMch;
import com.xpay.starter.plugin.plugins.DistributedLock;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

/**
 * 账户管理的业务逻辑层
 * @author chenyf
 * 
 */
@Component
public class AccountManageBiz {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccountMchDao accountMchDao;
	@Autowired
	private AccountProcessHandler accountProcessHandler;
	@Autowired
	DistributedLock<RLock> distributedLock;

	/**
	 * 创建账户
	 * @param accountNo 用户编号
	 * @throws AccountMchBizException
	 */
	public long createAccount(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio, String operator) throws AccountMchBizException {
		logger.info("创建账户 accountNo={} maxAdvanceAmount={} advanceRatio={} operator={}", accountNo, maxAdvanceAmount, advanceRatio, operator);

		advanceRatioValid(accountNo, maxAdvanceAmount, advanceRatio);

		AccountMch account = accountMchDao.getByAccountNo(accountNo);
		if(account != null){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo对应的账户记录已存在");
		}

		account = new AccountMch();
		account.setCreateTime(new Date());
		account.setVersion(0L);
		account.setAccountNo(accountNo);
		account.setStatus(AccountStatusEnum.ACTIVE.getValue());
		account.setBalance(BigDecimal.ZERO);
		account.setSettledAmount(BigDecimal.ZERO);
		account.setUnsettleAmount(BigDecimal.ZERO);
		account.setRsmAmount(BigDecimal.ZERO);
		account.setTotalAdvanceAmount(BigDecimal.ZERO);
		account.setAvailAdvanceAmount(BigDecimal.ZERO);
		account.setRetainAdvanceAmount(BigDecimal.ZERO);
		account.setCurrentAdvanceAmount(BigDecimal.ZERO);
		account.setTotalCreditAmount(BigDecimal.ZERO);
		account.setTotalDebitAmount(BigDecimal.ZERO);
		account.setTotalReturnAmount(BigDecimal.ZERO);
		account.setTotalAdvanceDebitAmount(BigDecimal.ZERO);
		account.setTotalAdvanceReturnAmount(BigDecimal.ZERO);
		account.setTotalOrderCount(AccountingConfig.DEFAULT_TOTAL_ORDER_COUNT);
		account.setTotalCrossReturnAmount(BigDecimal.ZERO);
		account.setAdvanceRatio(advanceRatio);
		account.setMaxAvailAdvanceAmount(maxAdvanceAmount);
		account.setNextSnapTime(DateUtil.getDayStartSecond(new Date()));
		account.setNextLiquidateTime(DateUtil.getDayStartSecond(new Date()));
		account.setLiquidateVersion(0);

		accountMchDao.insert(account);
		return account.getId();
	}

	/**
	 * 账户状态变更操作.
	 *
	 * @param accountNo 账户编号.
	 * @param accountStatus 账户状态
	 * @param desc 变更操作说明.
	 */
	public void changeAccountStatus(String accountNo, AccountStatusEnum accountStatus, String desc) throws AccountMchBizException {
		if(StringUtil.isEmpty(accountNo)){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo不能为空");
		}else if(accountStatus == null){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountStatus不能为空");
		}

		RLock lock = null;
		try {
			String lockName = AccountProcessHandler.getLockName(accountNo);
			lock = distributedLock.tryLock(lockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);

			AccountMch account = accountMchDao.getByAccountNo(accountNo);
			if(account == null){
				throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo对应的账户记录已存在");
			}

			if(accountStatus.getValue() == AccountStatusEnum.CANCELLED.getValue()){
				if(AmountUtil.greater(account.getBalance(), BigDecimal.ZERO)){
					throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "该账户还有余额，不可注销");
				}
			}

			if(account.getStatus() == AccountStatusEnum.FREEZE_CREDIT.getValue() && accountStatus.getValue() == AccountStatusEnum.FREEZE_DEBIT.getValue()){
				//如果旧状态为冻结止收，新状态为冻结止付，则直接把账户状态变为 冻结中
				account.setStatus(AccountStatusEnum.FREEZING.getValue());
			}else if(account.getStatus() == AccountStatusEnum.FREEZE_DEBIT.getValue() && accountStatus.getValue() == AccountStatusEnum.FREEZE_CREDIT.getValue()){
				//如果旧状态为冻结止付，新状态为冻结止收，则直接把账户状态变为 冻结中
				account.setStatus(AccountStatusEnum.FREEZING.getValue());
			}else{
				account.setStatus(accountStatus.getValue());
			}

			logger.info("变更账户状态 accountNo={} accountStatus={} AccountMch.status={} desc={}", accountNo, accountStatus, account.getStatus(), desc);
			accountMchDao.update(account);
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
	 * 修改下次余额快照时间
	 * @param accountNo
	 * @param nextSnapTime
	 */
	public void changeNextSnapTime(String accountNo, int nextSnapTime){
		if(StringUtil.isEmpty(accountNo)){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo不能为空");
		}
		if (System.currentTimeMillis() < DateUtil.secondToMillSecond(nextSnapTime)){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "nextSnapTime不能小于当前时间");
		}

		RLock lock = null;
		try {
			String lockName = AccountProcessHandler.getLockName(accountNo);
			lock = distributedLock.tryLock(lockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);

			AccountMch account = accountMchDao.getByAccountNo(accountNo);
			if(account == null){
				throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo对应的账户记录已存在");
			}

			logger.info("变更下次余额快照时间 accountNo={} nextSnapTime={} ", accountNo, nextSnapTime);
			account.setNextSnapTime(nextSnapTime);
			accountMchDao.update(account);
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
	 * 修改下次清零时间
	 * @param accountNo
	 * @param nextLiquidateTime
	 */
	public void changeNextClearTime(String accountNo, int nextLiquidateTime){
		if(StringUtil.isEmpty(accountNo)){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo不能为空");
		}
		if (System.currentTimeMillis() < DateUtil.secondToMillSecond(nextLiquidateTime)){
			throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "nextLiquidateTime不能小于当前时间");
		}

		RLock lock = null;
		try {
			String lockName = AccountProcessHandler.getLockName(accountNo);
			lock = distributedLock.tryLock(lockName, DistLockConst.ACCOUNT_LOCK_WAIT_MILLS, DistLockConst.ACCOUNT_LOCK_EXPIRE_MILLS);

			AccountMch account = accountMchDao.getByAccountNo(accountNo);
			if(account == null){
				throw new AccountMchBizException(AccountMchBizException.BIZ_INVALID, "accountNo对应的账户记录已存在");
			}

			logger.info("变更下次清零时间 accountNo={} nextLiquidateTime={} ", accountNo, nextLiquidateTime);
			account.setNextLiquidateTime(nextLiquidateTime);
			accountMchDao.update(account);
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
	 * @description:     调整垫资比例，包活调整最大垫资额度和垫资比例
	 * @date:            2019/09/05
	 */
	public void adjustAdvanceRatio(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio, String operator, String desc) {
		logger.info("accountNo={} maxAdvanceAmount={} advanceRatio={} operator={} desc={}", accountNo, maxAdvanceAmount, advanceRatio, operator, desc);

		advanceRatioValid(accountNo, maxAdvanceAmount, advanceRatio);

		AccountRequestDto requestDto = new AccountRequestDto();

		AccountProcessDto processDto = new AccountProcessDto();
		processDto.setAccountNo(accountNo);
		processDto.setAmount(maxAdvanceAmount);
		processDto.setFee(advanceRatio);
		processDto.setTrxNo(RandomUtil.get16LenStr());//生成一个随机流水号
		processDto.setMchTrxNo(processDto.getTrxNo());
		processDto.setProcessType(AccountProcessTypeEnum.ADJUST_AMOUNT_RATIO.getValue());
		processDto.setAmountType(AccountMchAmountTypeEnum.TOTAL_ADVANCE_AMOUNT.getValue());
		processDto.setBussType(ProductTypeEnum.LIQUIDATION.getValue());
		processDto.setBussCode(ProductCodeEnum.ADVANCE_ADJUST.getValue());
		processDto.setDesc(desc == null ? "" : desc);
		accountProcessHandler.process(requestDto, Collections.singletonList(processDto));
	}

	private void advanceRatioValid(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio){
		if(StringUtil.isEmpty(accountNo)){
			throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "accountNo不能为空");
		}else if(maxAdvanceAmount == null){
			throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "maxAdvanceAmount不能为空");
		}else if(AmountUtil.lessThan(advanceRatio, BigDecimal.ZERO)){
			throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "maxAdvanceAmount须大于0");
		}else if(advanceRatio == null){
			throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "advanceRatio不能为空");
		}else if(AmountUtil.lessThan(advanceRatio, BigDecimal.ZERO) || AmountUtil.greater(advanceRatio, BigDecimal.ONE)){
			throw new AccountMchBizException(AccountMchBizException.PARAM_INVALID, "advanceRatio须在0~1之间");
		}
	}
}
