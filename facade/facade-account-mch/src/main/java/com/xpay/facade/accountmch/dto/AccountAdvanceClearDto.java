package com.xpay.facade.accountmch.dto;

import java.io.Serializable;

/**
 * 账户垫资清零表
 */
public class AccountAdvanceClearDto implements Serializable {
	private static final long serialVersionUID = 1L;

	//columns START
	/**
	 * 自增id(主键)
	 */
	private Long id;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 创建日期
	 */
	private java.util.Date createDate;
	/**
	 * 清零流水号
	 */
	private String clearNo;
	/**
	 * 账户编号
	 */
	private String accountNo;
	/**
	 * 余额
	 */
	private java.math.BigDecimal balance;
	/**
	 * 可结算金额
	 */
	private java.math.BigDecimal settleAmount;
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
	 * 垫资收单总额
	 */
	private java.math.BigDecimal totalCreditAmount;
	/**
	 * 垫资退回总额
	 */
	private java.math.BigDecimal totalReturnAmount;
	/**
	 * 垫资出款总额
	 */
	private java.math.BigDecimal totalDebitAmount;
	/**
	 * 结余金额
	 */
	private java.math.BigDecimal remainAmount;
	/**
	 * 垫资收入总笔数
	 */
	private Integer totalReceiveCount;
	/**
	 * 垫资退回总笔数
	 */
	private Integer totalReturnCount;
	/**
	 * 垫资出款总笔数
	 */
	private Integer totalDebitCount;
	/**
	 * 垫资比例
	 */
	private java.math.BigDecimal advanceRatio;
	/**
	 * 最大垫资金额
	 */
	private java.math.BigDecimal maxAdvanceAmount;
	/**
	 * 清零账务明细id
	 */
	private Long clearAccountDetailId;
	/**
	 * 清算场次
	 */
	private java.util.Date clearRound;
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
	 * 创建日期
	 */
	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}
	/**
	 * 创建日期
	 */
	public java.util.Date getCreateDate() {
		return this.createDate;
	}
	/**
	 * 清零流水号
	 */
	public void setClearNo(String clearNo) {
		this.clearNo = clearNo;
	}
	/**
	 * 清零流水号
	 */
	public String getClearNo() {
		return this.clearNo;
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
	 * 余额
	 */
	public void setBalance(java.math.BigDecimal balance) {
		this.balance = balance;
	}
	/**
	 * 余额
	 */
	public java.math.BigDecimal getBalance() {
		return this.balance;
	}
	/**
	 * 可结算金额
	 */
	public void setSettleAmount(java.math.BigDecimal settleAmount) {
		this.settleAmount = settleAmount;
	}
	/**
	 * 可结算金额
	 */
	public java.math.BigDecimal getSettleAmount() {
		return this.settleAmount;
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
	 * 垫资收单总额
	 */
	public void setTotalCreditAmount(java.math.BigDecimal totalCreditAmount) {
		this.totalCreditAmount = totalCreditAmount;
	}
	/**
	 * 垫资收单总额
	 */
	public java.math.BigDecimal getTotalCreditAmount() {
		return this.totalCreditAmount;
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
	 * 结余金额
	 */
	public void setRemainAmount(java.math.BigDecimal remainAmount) {
		this.remainAmount = remainAmount;
	}
	/**
	 * 结余金额
	 */
	public java.math.BigDecimal getRemainAmount() {
		return this.remainAmount;
	}
	/**
	 * 垫资收入总笔数
	 */
	public void setTotalReceiveCount(Integer totalReceiveCount) {
		this.totalReceiveCount = totalReceiveCount;
	}
	/**
	 * 垫资收入总笔数
	 */
	public Integer getTotalReceiveCount() {
		return this.totalReceiveCount;
	}
	/**
	 * 垫资退回总笔数
	 */
	public void setTotalReturnCount(Integer totalReturnCount) {
		this.totalReturnCount = totalReturnCount;
	}
	/**
	 * 垫资退回总笔数
	 */
	public Integer getTotalReturnCount() {
		return this.totalReturnCount;
	}
	/**
	 * 垫资出款总笔数
	 */
	public void setTotalDebitCount(Integer totalDebitCount) {
		this.totalDebitCount = totalDebitCount;
	}
	/**
	 * 垫资出款总笔数
	 */
	public Integer getTotalDebitCount() {
		return this.totalDebitCount;
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
	/**
	 * 清零账务明细id
	 */
	public void setClearAccountDetailId(Long clearAccountDetailId) {
		this.clearAccountDetailId = clearAccountDetailId;
	}
	/**
	 * 清零账务明细id
	 */
	public Long getClearAccountDetailId() {
		return this.clearAccountDetailId;
	}
	/**
	 * 清算场次
	 */
	public void setClearRound(java.util.Date clearRound) {
		this.clearRound = clearRound;
	}
	/**
	 * 清算场次
	 */
	public java.util.Date getClearRound() {
		return this.clearRound;
	}

}
