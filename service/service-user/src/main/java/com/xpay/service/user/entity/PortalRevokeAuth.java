/*
 * Powered By [xpay.com]
 */
package com.xpay.service.user.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 商户功能回收记录表
 */
public class PortalRevokeAuth {

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
	 * 创建人
	 */
	private String creator;
	/**
	 * 回收类型
	 */
	private Integer revokeType;
	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 被回收权限的商户编号(json数组)
	 */
	private String mchNos;
	/**
	 * 当前已回收到的商户编号
	 */
	private String currMchNo;
	/**
	 * 被操作对象
	 */
	private String objectKey;
	/**
	 * remark
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
	 * 创建人
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	/**
	 * 创建人
	 */
	public String getCreator() {
		return this.creator;
	}
	/**
	 * 回收类型
	 */
	public void setRevokeType(Integer revokeType) {
		this.revokeType = revokeType;
	}
	/**
	 * 回收类型
	 */
	public Integer getRevokeType() {
		return this.revokeType;
	}
	/**
	 * 状态
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 状态
	 */
	public Integer getStatus() {
		return this.status;
	}
	/**
	 * 被回收权限的商户编号(json数组)
	 */
	public void setMchNos(String mchNos) {
		this.mchNos = mchNos;
	}
	/**
	 * 被回收权限的商户编号(json数组)
	 */
	public String getMchNos() {
		return this.mchNos;
	}
	/**
	 * 当前已回收到的商户编号
	 */
	public void setCurrMchNo(String currMchNo) {
		this.currMchNo = currMchNo;
	}
	/**
	 * 当前已回收到的商户编号
	 */
	public String getCurrMchNo() {
		return this.currMchNo;
	}
	/**
	 * 被操作对象
	 */
	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}
	/**
	 * 被操作对象
	 */
	public String getObjectKey() {
		return this.objectKey;
	}
	/**
	 * remark
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * remark
	 */
	public String getRemark() {
		return this.remark;
	}

}
