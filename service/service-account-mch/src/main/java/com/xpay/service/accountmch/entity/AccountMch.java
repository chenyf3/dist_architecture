package com.xpay.service.accountmch.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 商户账户表
 */
public class AccountMch implements Serializable {

	//columns START
	/**
	 * 自增主键
	 */
	@PK
	private Long id;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 版本号
	 */
	private Long version;
	/**
	 * 账户编号
	 */
	private String accountNo;
	/**
	 * 账户状态
	 */
	private Integer status;
	/**
	 * 账户总余额
	 */
	private java.math.BigDecimal balance;
	/**
	 * 已结算金额
	 */
	private java.math.BigDecimal settledAmount;
	/**
	 * 待结算金额
	 */
	private java.math.BigDecimal unsettleAmount;
	/**
	 * 风控金额
	 */
	private java.math.BigDecimal rsmAmount;
	/**
	 * 总垫资金额(待清算金额)
	 */
	private java.math.BigDecimal totalAdvanceAmount;
	/**
	 * 可用垫资金额
	 */
	private java.math.BigDecimal availAdvanceAmount;
	/**
	 * 留存垫资金额
	 */
	private java.math.BigDecimal retainAdvanceAmount;
	/**
	 * 当前垫资额度
	 */
	private java.math.BigDecimal currentAdvanceAmount;
	/**
	 * 垫资收入总额
	 */
	private java.math.BigDecimal totalCreditAmount;
	/**
	 * 垫资出款总额
	 */
	private java.math.BigDecimal totalDebitAmount;
	/**
	 * 垫资退回总额
	 */
	private java.math.BigDecimal totalReturnAmount;
	/**
	 * 可用垫资出款总额
	 */
	private java.math.BigDecimal totalAdvanceDebitAmount;
	/**
	 * 可用垫资退回总额
	 */
	private java.math.BigDecimal totalAdvanceReturnAmount;
	/**
	 * 垫资订单总数(收单,出款,退回)
	 */
	private String totalOrderCount;
	/**
	 * 隔日退回总额
	 */
	private java.math.BigDecimal totalCrossReturnAmount;
	/**
	 * 垫资比例
	 */
	private java.math.BigDecimal advanceRatio;
	/**
	 * 最大垫资金额
	 */
	private java.math.BigDecimal maxAvailAdvanceAmount;
	/**
	 * 下次快照时间
	 */
	private Integer nextSnapTime;
	/**
	 * 下次清算时间
	 */
	private Integer nextLiquidateTime;
	/**
	 * 清算版本号
	 */
	private Integer liquidateVersion;
	//columns END


	/**
	 * 自增主键
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 自增主键
	 */
	public Long getId() {
		return this.id;
	}
	/**
	 * 创建时间
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * 创建时间
	 */
	public java.util.Date getCreateTime() {
		return this.createTime;
	}
	/**
	 * 版本号
	 */
	public void setVersion(Long version) {
		this.version = version;
	}
	/**
	 * 版本号
	 */
	public Long getVersion() {
		return this.version;
	}
	/**
	 * 账户编号
	 */
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	/**
	 * 账户编号
	 */
	public String getAccountNo() {
		return this.accountNo;
	}
	/**
	 * 账户状态
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 账户状态
	 */
	public Integer getStatus() {
		return this.status;
	}
	/**
	 * 账户总余额
	 */
	public void setBalance(java.math.BigDecimal balance) {
		this.balance = balance;
	}
	/**
	 * 账户总余额
	 */
	public java.math.BigDecimal getBalance() {
		return this.balance;
	}
	/**
	 * 已结算金额
	 */
	public void setSettledAmount(java.math.BigDecimal settledAmount) {
		this.settledAmount = settledAmount;
	}
	/**
	 * 已结算金额
	 */
	public java.math.BigDecimal getSettledAmount() {
		return this.settledAmount;
	}
	/**
	 * 待结算金额
	 */
	public void setUnsettleAmount(java.math.BigDecimal unsettleAmount) {
		this.unsettleAmount = unsettleAmount;
	}
	/**
	 * 待结算金额
	 */
	public java.math.BigDecimal getUnsettleAmount() {
		return this.unsettleAmount;
	}
	/**
	 * 风控金额
	 */
	public void setRsmAmount(java.math.BigDecimal rsmAmount) {
		this.rsmAmount = rsmAmount;
	}
	/**
	 * 风控金额
	 */
	public java.math.BigDecimal getRsmAmount() {
		return this.rsmAmount;
	}
	/**
	 * 总垫资金额(待清算金额)
	 */
	public void setTotalAdvanceAmount(java.math.BigDecimal totalAdvanceAmount) {
		this.totalAdvanceAmount = totalAdvanceAmount;
	}
	/**
	 * 总垫资金额(待清算金额)
	 */
	public java.math.BigDecimal getTotalAdvanceAmount() {
		return this.totalAdvanceAmount;
	}
	/**
	 * 可用垫资金额
	 */
	public void setAvailAdvanceAmount(java.math.BigDecimal availAdvanceAmount) {
		this.availAdvanceAmount = availAdvanceAmount;
	}
	/**
	 * 可用垫资金额
	 */
	public java.math.BigDecimal getAvailAdvanceAmount() {
		return this.availAdvanceAmount;
	}
	/**
	 * 留存垫资金额
	 */
	public void setRetainAdvanceAmount(java.math.BigDecimal retainAdvanceAmount) {
		this.retainAdvanceAmount = retainAdvanceAmount;
	}
	/**
	 * 留存垫资金额
	 */
	public java.math.BigDecimal getRetainAdvanceAmount() {
		return this.retainAdvanceAmount;
	}
	/**
	 * 当前垫资额度
	 */
	public void setCurrentAdvanceAmount(java.math.BigDecimal currentAdvanceAmount) {
		this.currentAdvanceAmount = currentAdvanceAmount;
	}
	/**
	 * 当前垫资额度
	 */
	public java.math.BigDecimal getCurrentAdvanceAmount() {
		return this.currentAdvanceAmount;
	}
	/**
	 * 垫资收入总额
	 */
	public void setTotalCreditAmount(java.math.BigDecimal totalCreditAmount) {
		this.totalCreditAmount = totalCreditAmount;
	}
	/**
	 * 垫资收入总额
	 */
	public java.math.BigDecimal getTotalCreditAmount() {
		return this.totalCreditAmount;
	}
	/**
	 * 垫资出款总额
	 */
	public void setTotalDebitAmount(java.math.BigDecimal totalDebitAmount) {
		this.totalDebitAmount = totalDebitAmount;
	}
	/**
	 * 垫资出款总额
	 */
	public java.math.BigDecimal getTotalDebitAmount() {
		return this.totalDebitAmount;
	}
	/**
	 * 垫资退回总额
	 */
	public void setTotalReturnAmount(java.math.BigDecimal totalReturnAmount) {
		this.totalReturnAmount = totalReturnAmount;
	}
	/**
	 * 垫资退回总额
	 */
	public java.math.BigDecimal getTotalReturnAmount() {
		return this.totalReturnAmount;
	}
	/**
	 * 可用垫资出款总额
	 */
	public void setTotalAdvanceDebitAmount(java.math.BigDecimal totalAdvanceDebitAmount) {
		this.totalAdvanceDebitAmount = totalAdvanceDebitAmount;
	}
	/**
	 * 可用垫资出款总额
	 */
	public java.math.BigDecimal getTotalAdvanceDebitAmount() {
		return this.totalAdvanceDebitAmount;
	}
	/**
	 * 可用垫资退回总额
	 */
	public void setTotalAdvanceReturnAmount(java.math.BigDecimal totalAdvanceReturnAmount) {
		this.totalAdvanceReturnAmount = totalAdvanceReturnAmount;
	}
	/**
	 * 可用垫资退回总额
	 */
	public java.math.BigDecimal getTotalAdvanceReturnAmount() {
		return this.totalAdvanceReturnAmount;
	}
	/**
	 * 垫资订单总数(收单,出款,退回)
	 */
	public void setTotalOrderCount(String totalOrderCount) {
		this.totalOrderCount = totalOrderCount;
	}
	/**
	 * 垫资订单总数(收单,出款,退回)
	 */
	public String getTotalOrderCount() {
		return this.totalOrderCount;
	}
	/**
	 * 隔日退回总额
	 */
	public void setTotalCrossReturnAmount(java.math.BigDecimal totalCrossReturnAmount) {
		this.totalCrossReturnAmount = totalCrossReturnAmount;
	}
	/**
	 * 隔日退回总额
	 */
	public java.math.BigDecimal getTotalCrossReturnAmount() {
		return this.totalCrossReturnAmount;
	}
	/**
	 * 垫资比例
	 */
	public void setAdvanceRatio(java.math.BigDecimal advanceRatio) {
		this.advanceRatio = advanceRatio;
	}
	/**
	 * 垫资比例
	 */
	public java.math.BigDecimal getAdvanceRatio() {
		return this.advanceRatio;
	}
	/**
	 * 最大垫资金额
	 */
	public void setMaxAvailAdvanceAmount(java.math.BigDecimal maxAvailAdvanceAmount) {
		this.maxAvailAdvanceAmount = maxAvailAdvanceAmount;
	}
	/**
	 * 最大垫资金额
	 */
	public java.math.BigDecimal getMaxAvailAdvanceAmount() {
		return this.maxAvailAdvanceAmount;
	}
	/**
	 * 下次快照时间
	 */
	public void setNextSnapTime(Integer nextSnapTime) {
		this.nextSnapTime = nextSnapTime;
	}
	/**
	 * 下次快照时间
	 */
	public Integer getNextSnapTime() {
		return this.nextSnapTime;
	}
	/**
	 * 下次清算时间
	 */
	public void setNextLiquidateTime(Integer nextLiquidateTime) {
		this.nextLiquidateTime = nextLiquidateTime;
	}
	/**
	 * 下次清算时间
	 */
	public Integer getNextLiquidateTime() {
		return this.nextLiquidateTime;
	}
	/**
	 * 清算版本号
	 */
	public void setLiquidateVersion(Integer liquidateVersion) {
		this.liquidateVersion = liquidateVersion;
	}
	/**
	 * 清算版本号
	 */
	public Integer getLiquidateVersion() {
		return this.liquidateVersion;
	}

}
