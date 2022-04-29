package com.xpay.service.merchant.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 商户表
 */
public class Merchant implements Serializable {

	//columns START
	/**
	 * id
	 */
	@PK
	private Long id;
	/**
	 * createTime
	 */
	private java.util.Date createTime;
	/**
	 * version
	 */
	private Integer version;
	/**
	 * mchNo
	 */
	private String mchNo;
	/**
	 * mchType
	 */
	private Integer mchType;
	/**
	 * fullName
	 */
	private String fullName;
	/**
	 * shortName
	 */
	private String shortName;
	/**
	 * status
	 */
	private Integer status;
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
	 * createTime
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * createTime
	 */
	public java.util.Date getCreateTime() {
		return this.createTime;
	}
	/**
	 * version
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
	/**
	 * version
	 */
	public Integer getVersion() {
		return this.version;
	}
	/**
	 * mchNo
	 */
	public void setMchNo(String mchNo) {
		this.mchNo = mchNo;
	}
	/**
	 * mchNo
	 */
	public String getMchNo() {
		return this.mchNo;
	}
	/**
	 * mchType
	 */
	public void setMchType(Integer mchType) {
		this.mchType = mchType;
	}
	/**
	 * mchType
	 */
	public Integer getMchType() {
		return this.mchType;
	}
	/**
	 * fullName
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	/**
	 * fullName
	 */
	public String getFullName() {
		return this.fullName;
	}
	/**
	 * shortName
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	/**
	 * shortName
	 */
	public String getShortName() {
		return this.shortName;
	}
	/**
	 * status
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * status
	 */
	public Integer getStatus() {
		return this.status;
	}

}
