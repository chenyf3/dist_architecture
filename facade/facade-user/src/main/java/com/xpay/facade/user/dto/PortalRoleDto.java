/*
 * Powered By [xpay.com]
 */
package com.xpay.facade.user.dto;

import java.io.Serializable;

/**
 * 商户角色表
 */
public class PortalRoleDto implements Serializable {
	private static final long serialVersionUID = 1L;

	//columns START
	/**
	 * ID
	 */
	private Long id;
	/**
	 * VERSION
	 */
	private Integer version;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 商户编码(ROLE_TYPE为1时赋值空字符串)
	 */
	private String mchNo;
	/**
	 * 商户类型
	 */
	private Integer mchType;
	/**
	 * 角色类型（1:模版角色，2:普通角色）
	 */
	private Integer roleType;
	/**
	 * 自动分配(-1:否 1:是)
	 */
	private Integer autoAssign;
	/**
	 * 角色名称
	 */
	private String roleName;
	/**
	 * 描述
	 */
	private String remark;
	//columns END


	/**
	 * ID
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * ID
	 */
	public Long getId() {
		return this.id;
	}
	/**
	 * VERSION
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
	/**
	 * VERSION
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
	 * 商户编码(ROLE_TYPE为1时赋值空字符串)
	 */
	public void setMchNo(String mchNo) {
		this.mchNo = mchNo;
	}
	/**
	 * 商户编码(ROLE_TYPE为1时赋值空字符串)
	 */
	public String getMchNo() {
		return this.mchNo;
	}
	/**
	 * 商户类型
	 */
	public void setMchType(Integer mchType) {
		this.mchType = mchType;
	}
	/**
	 * 商户类型
	 */
	public Integer getMchType() {
		return this.mchType;
	}
	/**
	 * 角色类型（1:模版角色，2:普通角色）
	 */
	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}
	/**
	 * 角色类型（1:模版角色，2:普通角色）
	 */
	public Integer getRoleType() {
		return this.roleType;
	}
	/**
	 * 自动分配(-1:否 1:是)
	 */
	public void setAutoAssign(Integer autoAssign) {
		this.autoAssign = autoAssign;
	}
	/**
	 * 自动分配(-1:否 1:是)
	 */
	public Integer getAutoAssign() {
		return this.autoAssign;
	}
	/**
	 * 角色名称
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	/**
	 * 角色名称
	 */
	public String getRoleName() {
		return this.roleName;
	}
	/**
	 * 描述
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 描述
	 */
	public String getRemark() {
		return this.remark;
	}

}
