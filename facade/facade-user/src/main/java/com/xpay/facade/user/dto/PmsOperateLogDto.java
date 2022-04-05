/*
 * Powered By [xpay.com]
 */
package com.xpay.facade.user.dto;

import java.io.Serializable;

/**
 * pms用户操作日志表
 */
public class PmsOperateLogDto implements Serializable {
	private static final long serialVersionUID = 1L;

	//columns START
	/**
	 * 主键ID
	 */
	private Long id;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 操作员登录名
	 */
	private String loginName;
	/**
	 * 操作类型（PmsOperateLogTypeEnum）
	 */
	private Integer operateType;
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
	 * 操作员登录名
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	/**
	 * 操作员登录名
	 */
	public String getLoginName() {
		return this.loginName;
	}
	/**
	 * 操作类型（PmsOperateLogTypeEnum）
	 */
	public void setOperateType(Integer operateType) {
		this.operateType = operateType;
	}
	/**
	 * 操作类型（PmsOperateLogTypeEnum）
	 */
	public Integer getOperateType() {
		return this.operateType;
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
