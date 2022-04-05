package com.xpay.facade.user.dto;

import java.io.Serializable;

/**
 * pms用户表
 */
public class PmsUserDto implements Serializable {
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
	 * 登录名
	 */
	private String loginName;
	/**
	 * 登录密码
	 */
	private String loginPwd;
	/**
	 * 描述
	 */
	private String remark;
	/**
	 * 真实姓名
	 */
	private String realName;
	/**
	 * 手机号码
	 */
	private String mobileNo;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 用户类型（1:超级管理员，2:普通用户）
	 */
	private Integer type;
	/**
	 * 创建者
	 */
	private String creator;
	/**
	 * 修改者
	 */
	private String modifier;
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
	 * 登录名
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	/**
	 * 登录名
	 */
	public String getLoginName() {
		return this.loginName;
	}
	/**
	 * 登录密码
	 */
	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}
	/**
	 * 登录密码
	 */
	public String getLoginPwd() {
		return this.loginPwd;
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
	/**
	 * 真实姓名
	 */
	public void setRealName(String realName) {
		this.realName = realName;
	}
	/**
	 * 真实姓名
	 */
	public String getRealName() {
		return this.realName;
	}
	/**
	 * 手机号码
	 */
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	/**
	 * 手机号码
	 */
	public String getMobileNo() {
		return this.mobileNo;
	}
	/**
	 * 邮箱
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * 邮箱
	 */
	public String getEmail() {
		return this.email;
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
	 * 用户类型（1:超级管理员，2:普通用户）
	 */
	public void setType(Integer type) {
		this.type = type;
	}
	/**
	 * 用户类型（1:超级管理员，2:普通用户）
	 */
	public Integer getType() {
		return this.type;
	}
	/**
	 * 创建者
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	/**
	 * 创建者
	 */
	public String getCreator() {
		return this.creator;
	}
	/**
	 * 修改者
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	/**
	 * 修改者
	 */
	public String getModifier() {
		return this.modifier;
	}

}
