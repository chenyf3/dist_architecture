package com.xpay.facade.merchant.dto;

import java.io.Serializable;

/**
 * 商户详细信息表
 */
public class MerchantDetailDto implements Serializable {
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
	 * 商户地址
	 */
	private String address;
	/**
	 * 商户网址
	 */
	private String url;
	/**
	 * 商户网站备案号
	 */
	private String icp;
	/**
	 * 固定电话
	 */
	private String telephone;
	/**
	 * 业务联系手机号
	 */
	private String bussMobileNo;
	/**
	 * 业务联系邮箱
	 */
	private String bussContactEmail;
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
	 * 商户地址
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * 商户地址
	 */
	public String getAddress() {
		return this.address;
	}
	/**
	 * 商户网址
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 商户网址
	 */
	public String getUrl() {
		return this.url;
	}
	/**
	 * 商户网站备案号
	 */
	public void setIcp(String icp) {
		this.icp = icp;
	}
	/**
	 * 商户网站备案号
	 */
	public String getIcp() {
		return this.icp;
	}
	/**
	 * 固定电话
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	/**
	 * 固定电话
	 */
	public String getTelephone() {
		return this.telephone;
	}
	/**
	 * 业务联系手机号
	 */
	public void setBussMobileNo(String bussMobileNo) {
		this.bussMobileNo = bussMobileNo;
	}
	/**
	 * 业务联系手机号
	 */
	public String getBussMobileNo() {
		return this.bussMobileNo;
	}
	/**
	 * 业务联系邮箱
	 */
	public void setBussContactEmail(String bussContactEmail) {
		this.bussContactEmail = bussContactEmail;
	}
	/**
	 * 业务联系邮箱
	 */
	public String getBussContactEmail() {
		return this.bussContactEmail;
	}

}
