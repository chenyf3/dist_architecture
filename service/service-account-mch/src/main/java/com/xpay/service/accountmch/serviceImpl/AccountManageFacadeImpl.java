package com.xpay.service.accountmch.serviceImpl;

import com.xpay.common.statics.enums.account.AccountStatusEnum;
import com.xpay.common.statics.exception.AccountMchBizException;
import com.xpay.facade.accountmch.service.AccountManageFacade;
import com.xpay.service.accountmch.biz.AccountManageBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @description: 平台商户账户管理
 * @author: chenyf
 * @date: 2019-09-05
 */
@DubboService
public class AccountManageFacadeImpl implements AccountManageFacade {
	@Autowired
	AccountManageBiz accountManageBiz;

	/**
	 * 创建账户
	 * @param accountNo 账户编号.
	 * @throws AccountMchBizException
	 */
	public long createAccount(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio, String operator) throws AccountMchBizException {
		return accountManageBiz.createAccount(accountNo, maxAdvanceAmount, advanceRatio, operator);
	}

	/**
	 * 账户状态变更操作.
	 * 
	 * @param accountNo 账户编号.
	 * @param accountStatus 账户状态
	 * @param desc 变更操作说明.
	 */
	public void changeAccountStatus(String accountNo, AccountStatusEnum accountStatus, String operator, String desc) throws AccountMchBizException {
		accountManageBiz.changeAccountStatus(accountNo, accountStatus, desc);
	}

	/**
	 * @description:     调整垫资比例，包活调整最大垫资额度和垫资比例
	 * @date:            2019/09/05
	 */
	public void adjustAdvanceRatio(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio, String operator,  String desc) {
		accountManageBiz.adjustAdvanceRatio(accountNo, maxAdvanceAmount, advanceRatio, operator, desc);
	}
}
