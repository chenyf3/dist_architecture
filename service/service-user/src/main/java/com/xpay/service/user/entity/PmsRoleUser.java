/*
 * Powered By [xpay.com]
 */
package com.xpay.service.user.entity;

import com.xpay.common.service.annotations.PK;

/**
 * pms操作员与角色关联表
 */
public class PmsRoleUser {

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
	 * 用户ID
	 */
	private Long userId;
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
	 * 用户ID
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	/**
	 * 用户ID
	 */
	public Long getUserId() {
		return this.userId;
	}

}
