/*
 * Powered By [xpay.com]
 */
package com.xpay.service.config.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 产品表
 */
public class Product implements Serializable {

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
	private Integer version;
	/**
	 * 业务线(产品类型)
	 */
	private Integer productType;
	/**
	 * 产品编号
	 */
	private Integer productCode;
	/**
	 * 状态(1=启用 2=禁用)
	 */
	private Integer status;
	/**
	 * 备注
	 */
	private String remark;
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
	 * 业务线(产品类型)
	 */
	public void setProductType(Integer productType) {
		this.productType = productType;
	}
	/**
	 * 业务线(产品类型)
	 */
	public Integer getProductType() {
		return this.productType;
	}
	/**
	 * 产品编号
	 */
	public void setProductCode(Integer productCode) {
		this.productCode = productCode;
	}
	/**
	 * 产品编号
	 */
	public Integer getProductCode() {
		return this.productCode;
	}
	/**
	 * 状态(1=启用 2=禁用)
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 状态(1=启用 2=禁用)
	 */
	public Integer getStatus() {
		return this.status;
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

}
