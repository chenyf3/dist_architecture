package com.xpay.service.accountmch.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 账务处理明细归档表
 */
public class AccountProcessDetailHistory {

	//columns START
	/**
	 * 自增主键
	 */
	@PK
	private Long id;
	/**
	 * 版本号
	 */
	private Integer version;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 创建日期
	 */
	private java.util.Date createDate;
	/**
	 * 帐户编号
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
	 * 风控金额
	 */
	private java.math.BigDecimal rsmAmount;
	/**
	 * 总垫资金额
	 */
	private java.math.BigDecimal totalAdvanceAmount;
	/**
	 * 可用垫资额度
	 */
	private java.math.BigDecimal availAdvanceAmount;
	/**
	 * 留存垫资金额
	 */
	private java.math.BigDecimal retainAdvanceAmount;
	/**
	 * 总余额变动金额
	 */
	private java.math.BigDecimal alterBalance;
	/**
	 * 已结算变动金额
	 */
	private java.math.BigDecimal alterSettledAmount;
	/**
	 * 总垫资变动金额
	 */
	private java.math.BigDecimal alterTotalAdvanceAmount;
	/**
	 * 可用垫资变动金额
	 */
	private java.math.BigDecimal alterAdvanceAmount;
	/**
	 * 留存金额变动金额
	 */
	private java.math.BigDecimal alterRetainAmount;
	/**
	 * 账务处理流水号
	 */
	private String processNo;
	/**
	 * 交易流水号
	 */
	private String requestNo;
	/**
	 * 商户订单号
	 */
	private String mchRequestNo;
	/**
	 * 订单交易时间
	 */
	private java.util.Date trxTime;
	/**
	 * 交易金额(变动金额)
	 */
	private java.math.BigDecimal trxAmount;
	/**
	 * 手续费
	 */
	private java.math.BigDecimal fee;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 账务处理类型
	 */
	private Integer processType;
	/**
	 * 额外信息
	 */
	private String extraInfo;
	/**
	 * 是否可清算(1=是 -1=否)
	 */
	private Integer liquidation;
	/**
	 * 清算版本号
	 */
	private Integer liquidateVersion;
	/**
	 * 迁移时间
	 */
	private java.util.Date migrateTime;
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
	 * 帐户编号
	 */
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	/**
	 * 帐户编号
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
	public void setAvailAdvanceAmount(java.math.BigDecimal availAdvanceAmount) {
		this.availAdvanceAmount = availAdvanceAmount;
	}
	/**
	 * 可用垫资额度
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
	 * 总余额变动金额
	 */
	public void setAlterBalance(java.math.BigDecimal alterBalance) {
		this.alterBalance = alterBalance;
	}
	/**
	 * 总余额变动金额
	 */
	public java.math.BigDecimal getAlterBalance() {
		return this.alterBalance;
	}
	/**
	 * 已结算变动金额
	 */
	public void setAlterSettledAmount(java.math.BigDecimal alterSettledAmount) {
		this.alterSettledAmount = alterSettledAmount;
	}
	/**
	 * 已结算变动金额
	 */
	public java.math.BigDecimal getAlterSettledAmount() {
		return this.alterSettledAmount;
	}
	/**
	 * 总垫资变动金额
	 */
	public void setAlterTotalAdvanceAmount(java.math.BigDecimal alterTotalAdvanceAmount) {
		this.alterTotalAdvanceAmount = alterTotalAdvanceAmount;
	}
	/**
	 * 总垫资变动金额
	 */
	public java.math.BigDecimal getAlterTotalAdvanceAmount() {
		return this.alterTotalAdvanceAmount;
	}
	/**
	 * 可用垫资变动金额
	 */
	public void setAlterAdvanceAmount(java.math.BigDecimal alterAdvanceAmount) {
		this.alterAdvanceAmount = alterAdvanceAmount;
	}
	/**
	 * 可用垫资变动金额
	 */
	public java.math.BigDecimal getAlterAdvanceAmount() {
		return this.alterAdvanceAmount;
	}
	/**
	 * 留存金额变动金额
	 */
	public void setAlterRetainAmount(java.math.BigDecimal alterRetainAmount) {
		this.alterRetainAmount = alterRetainAmount;
	}
	/**
	 * 留存金额变动金额
	 */
	public java.math.BigDecimal getAlterRetainAmount() {
		return this.alterRetainAmount;
	}
	/**
	 * 账务处理流水号
	 */
	public void setProcessNo(String processNo) {
		this.processNo = processNo;
	}
	/**
	 * 账务处理流水号
	 */
	public String getProcessNo() {
		return this.processNo;
	}
	/**
	 * 交易流水号
	 */
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}
	/**
	 * 交易流水号
	 */
	public String getRequestNo() {
		return this.requestNo;
	}
	/**
	 * 商户订单号
	 */
	public void setMchRequestNo(String mchRequestNo) {
		this.mchRequestNo = mchRequestNo;
	}
	/**
	 * 商户订单号
	 */
	public String getMchRequestNo() {
		return this.mchRequestNo;
	}
	/**
	 * 订单交易时间
	 */
	public void setTrxTime(java.util.Date trxTime) {
		this.trxTime = trxTime;
	}
	/**
	 * 订单交易时间
	 */
	public java.util.Date getTrxTime() {
		return this.trxTime;
	}
	/**
	 * 交易金额(变动金额)
	 */
	public void setTrxAmount(java.math.BigDecimal trxAmount) {
		this.trxAmount = trxAmount;
	}
	/**
	 * 交易金额(变动金额)
	 */
	public java.math.BigDecimal getTrxAmount() {
		return this.trxAmount;
	}
	/**
	 * 手续费
	 */
	public void setFee(java.math.BigDecimal fee) {
		this.fee = fee;
	}
	/**
	 * 手续费
	 */
	public java.math.BigDecimal getFee() {
		return this.fee;
	}
	/**
	 * 备注
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 备注
	 */
	public String getRemark() {
		return this.remark;
	}
	/**
	 * 账务处理类型
	 */
	public void setProcessType(Integer processType) {
		this.processType = processType;
	}
	/**
	 * 账务处理类型
	 */
	public Integer getProcessType() {
		return this.processType;
	}
	/**
	 * 额外信息
	 */
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	/**
	 * 额外信息
	 */
	public String getExtraInfo() {
		return this.extraInfo;
	}
	/**
	 * 是否可清算(1=是 -1=否)
	 */
	public void setLiquidation(Integer liquidation) {
		this.liquidation = liquidation;
	}
	/**
	 * 是否可清算(1=是 -1=否)
	 */
	public Integer getLiquidation() {
		return this.liquidation;
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
	/**
	 * 迁移时间
	 */
	public void setMigrateTime(java.util.Date migrateTime) {
		this.migrateTime = migrateTime;
	}
	/**
	 * 迁移时间
	 */
	public java.util.Date getMigrateTime() {
		return this.migrateTime;
	}

}
