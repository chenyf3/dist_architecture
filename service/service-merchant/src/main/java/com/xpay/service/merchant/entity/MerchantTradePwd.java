/*
 * Powered By [xpay.com]
 */
package com.xpay.service.merchant.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 商户交易密码表
 */
public class MerchantTradePwd {

	//columns START
	/**
	 * 自增ID
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
	 * 商户编号
	 */
	private String mchNo;
	/**
	 * 交易密码(密文)
	 */
	private String tradePwd;
	/**
	 * 密码错误次数
	 */
	private Integer errorTimes;
	/**
	 * 最后一次输错密码时间
	 */
	private java.util.Date lastErrorTime;
	/**
	 * 是否初始密码(1:是, -1:否)
	 */
	private Integer initial;
	//columns END


	/**
	 * 自增ID
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 自增ID
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
	 * 商户编号
	 */
	public void setMchNo(String mchNo) {
		this.mchNo = mchNo;
	}
	/**
	 * 商户编号
	 */
	public String getMchNo() {
		return this.mchNo;
	}
	/**
	 * 交易密码(密文)
	 */
	public void setTradePwd(String tradePwd) {
		this.tradePwd = tradePwd;
	}
	/**
	 * 交易密码(密文)
	 */
	public String getTradePwd() {
		return this.tradePwd;
	}
	/**
	 * 密码错误次数
	 */
	public void setErrorTimes(Integer errorTimes) {
		this.errorTimes = errorTimes;
	}
	/**
	 * 密码错误次数
	 */
	public Integer getErrorTimes() {
		return this.errorTimes;
	}
	/**
	 * 最后一次输错密码时间
	 */
	public void setLastErrorTime(java.util.Date lastErrorTime) {
		this.lastErrorTime = lastErrorTime;
	}
	/**
	 * 最后一次输错密码时间
	 */
	public java.util.Date getLastErrorTime() {
		return this.lastErrorTime;
	}
	/**
	 * 是否初始密码(1:是, -1:否)
	 */
	public void setInitial(Integer initial) {
		this.initial = initial;
	}
	/**
	 * 是否初始密码(1:是, -1:否)
	 */
	public Integer getInitial() {
		return this.initial;
	}

}
