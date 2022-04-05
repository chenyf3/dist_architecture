package com.xpay.facade.merchant.dto;

import java.io.Serializable;

/**
 * 商户密钥表
 */
public class MerchantSecretDto implements Serializable {
	private static final long serialVersionUID = 1L;

	//columns START
	/**
	 * id
	 */
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
	 * 修改时间
	 */
	private java.util.Date modifyTime;
	/**
	 * 商户编号
	 */
	private String mchNo;
	/**
	 * 密钥信息(json数组)
	 */
	private String secretKeys;
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
	 * 修改时间
	 */
	public void setModifyTime(java.util.Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	/**
	 * 修改时间
	 */
	public java.util.Date getModifyTime() {
		return this.modifyTime;
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
	 * 密钥信息(json数组)
	 */
	public void setSecretKeys(String secretKeys) {
		this.secretKeys = secretKeys;
	}
	/**
	 * 密钥信息(json数组)
	 */
	public String getSecretKeys() {
		return this.secretKeys;
	}
}
