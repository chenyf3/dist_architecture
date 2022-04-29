/*
 * Powered By [xpay.com]
 */
package com.xpay.service.user.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 商户角色与功能关联表
 */
public class PortalRoleAuth implements Serializable {

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
	/**
	 * 商户编号
	 */
	private String mchNo;
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
	 * 权限ID
	 */
	public void setAuthId(Long authId) {
		this.authId = authId;
	}
	/**
	 * 权限ID
	 */
	public Long getAuthId() {
		return this.authId;
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

}
