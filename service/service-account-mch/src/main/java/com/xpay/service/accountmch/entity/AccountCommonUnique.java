package com.xpay.service.accountmch.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 账务唯一约束表
 */
public class AccountCommonUnique {

	//columns START
	/**
	 * id
	 */
	@PK
	private Long id;
	/**
	 * 唯一约束键(定长32位)
	 */
	private String uniqueKey;
	/**
	 * 创建日期
	 */
	private java.util.Date createDate;
	//columns END


	/**
	 * id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * id
	 */
	public Long getId() {
		return this.id;
	}
	/**
	 * 唯一约束键(定长32位)
	 */
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	/**
	 * 唯一约束键(定长32位)
	 */
	public String getUniqueKey() {
		return this.uniqueKey;
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

}
