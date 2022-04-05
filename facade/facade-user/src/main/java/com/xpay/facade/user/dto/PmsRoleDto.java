package com.xpay.facade.user.dto;

import java.io.Serializable;

/**
 * pms角色表
 */
public class PmsRoleDto implements Serializable {
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
