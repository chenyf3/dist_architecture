package com.xpay.service.accountmch.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 账户余额快照
 */
public class AccountBalanceSnap implements Serializable {

	//columns START
	/**
	 * 自增id(主键)
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
	private Integer version;
	/**
	 * 快照日期
	 */
	private java.util.Date snapDate;
	/**
	 * 快照流水号
	 */
	private String snapNo;
	/**
	 * 账户编号
	 */
	private String accountNo;
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
	 * 总垫资金额
	 */
	private java.math.BigDecimal totalAdvanceAmount;
	/**
	 * 可用垫资额度
	 */
	private java.math.BigDecimal advanceAmount;
	/**
	 * 留存垫资金额
	 */
	private java.math.BigDecimal retainAmount;
	/**
	 * 风控金额
	 */
	private java.math.BigDecimal rsmAmount;
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
	 * 垫资订单总数(收单,出款,退回)
	 */
	private String totalOrderCount;
	/**
	 * 垫资比例
	 */
	private java.math.BigDecimal advanceRatio;
	/**
	 * 最大垫资金额
	 */
	private java.math.BigDecimal maxAdvanceAmount;
	//columns END


	/**
	 * 自增id(主键)
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 自增id(主键)
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
	public void setVersion(Integer version) {
		this.version = version;
	}
	/**
	 * 版本号
	 */
	public Integer getVersion() {
		return this.version;
	}
	/**
	 * 快照日期
	 */
	public void setSnapDate(java.util.Date snapDate) {
		this.snapDate = snapDate;
	}
	/**
	 * 快照日期
	 */
	public java.util.Date getSnapDate() {
		return this.snapDate;
	}
	/**
	 * 快照流水号
	 */
	public void setSnapNo(String snapNo) {
		this.snapNo = snapNo;
	}
	/**
	 * 快照流水号
	 */
	public String getSnapNo() {
		return this.snapNo;
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
	 * 总垫资金额
	 */
	public void setTotalAdvanceAmount(java.math.BigDecimal totalAdvanceAmount) {
		this.totalAdvanceAmount = totalAdvanceAmount;
	}
	/**
	 * 总垫资金额
	 */
	public java.math.BigDecimal getTotalAdvanceAmount() {
		return this.totalAdvanceAmount;
	}
	/**
	 * 可用垫资额度
	 */
	public void setAdvanceAmount(java.math.BigDecimal advanceAmount) {
		this.advanceAmount = advanceAmount;
	}
	/**
	 * 可用垫资额度
	 */
	public java.math.BigDecimal getAdvanceAmount() {
		return this.advanceAmount;
	}
	/**
	 * 留存垫资金额
	 */
	public void setRetainAmount(java.math.BigDecimal retainAmount) {
		this.retainAmount = retainAmount;
	}
	/**
	 * 留存垫资金额
	 */
	public java.math.BigDecimal getRetainAmount() {
		return this.retainAmount;
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
	public void setMaxAdvanceAmount(java.math.BigDecimal maxAdvanceAmount) {
		this.maxAdvanceAmount = maxAdvanceAmount;
	}
	/**
	 * 最大垫资金额
	 */
	public java.math.BigDecimal getMaxAdvanceAmount() {
		return this.maxAdvanceAmount;
	}

}
