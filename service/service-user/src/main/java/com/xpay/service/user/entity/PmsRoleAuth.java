/*
 * Powered By [xpay.com]
 */
package com.xpay.service.user.entity;

import com.xpay.common.service.annotations.PK;

/**
 * pms角色与权限关联表
 */
public class PmsRoleAuth {

	//columns START
	/**
	 * ID
	 */
	@PK
	private Long id;
	/**
	 * 角色ID
	 */
	private Long roleId;
	/**
	 * 功能ID
	 */
	private Long authId;
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
	 * 角色ID
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
	/**
	 * 角色ID
	 */
	public Long getRoleId() {
		return this.roleId;
	}
	/**
	 * 功能ID
	 */
	public void setAuthId(Long authId) {
		this.authId = authId;
	}
	/**
	 * 功能ID
	 */
	public Long getAuthId() {
		return this.authId;
	}

}
