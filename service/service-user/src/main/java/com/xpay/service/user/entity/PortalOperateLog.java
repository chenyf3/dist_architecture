/*
 * Powered By [xpay.com]
 */
package com.xpay.service.user.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 商户操作日志表
 */
public class PortalOperateLog {

	//columns START
	/**
	 * 主键ID
	 */
	@PK
	private Long id;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 登录名
	 */
	private String loginName;
	/**
	 * 商户编号
	 */
	private String mchNo;
	/**
	 * 操作类型（OperateLogTypeEnum）
	 */
	private Integer operateType;
	/**
	 * 操作状态（1:成功，-1:失败）
	 */
	private Integer status;
	/**
	 * IP地址
	 */
	private String ip;
	/**
	 * 操作内容
	 */
	private String content;
	//columns END


	/**
	 * 主键ID
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 主键ID
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
	 * 操作类型（OperateLogTypeEnum）
	 */
	public void setOperateType(Integer operateType) {
		this.operateType = operateType;
	}
	/**
	 * 操作类型（OperateLogTypeEnum）
	 */
	public Integer getOperateType() {
		return this.operateType;
	}
	/**
	 * 操作状态（1:成功，-1:失败）
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 操作状态（1:成功，-1:失败）
	 */
	public Integer getStatus() {
		return this.status;
	}
	/**
	 * IP地址
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 * IP地址
	 */
	public String getIp() {
		return this.ip;
	}
	/**
	 * 操作内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 操作内容
	 */
	public String getContent() {
		return this.content;
	}

}
