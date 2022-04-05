package com.xpay.facade.accountmch.service;

import com.xpay.common.statics.enums.account.AccountStatusEnum;
import com.xpay.common.statics.exception.BizException;

import java.math.BigDecimal;

/**
 * @description: 平台商户账户管理
 * @author: chenyf
 * @date: 2019-09-05
 */
public interface AccountManageFacade {

	/**
	 * 创建账户
	 * @param accountNo 账户编号.
	 * @throws BizException
	 */
	public long createAccount(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio, String operator) throws BizException;

	/**
	 * 账户状态变更操作.
	 * 
	 * @param accountNo 账户编号.
	 * @param accountStatus 账户状态
	 * @param desc 变更操作说明.
	 */
	public void changeAccountStatus(String accountNo, AccountStatusEnum accountStatus, String operator, String desc) throws BizException;

	/**
	 * @description:     调整垫资比例，包活调整最大垫资额度和垫资比例
	 * @date:            2019/09/05
	 */
	public void adjustAdvanceRatio(String accountNo, BigDecimal maxAdvanceAmount, BigDecimal advanceRatio, String operator, String desc) throws BizException;
}
