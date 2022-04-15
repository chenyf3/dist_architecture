/*
 * Powered By [xpay.com]
 */
package com.xpay.service.message.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 邮件接收人
 */
public class MailGroup {

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
	 * 分组名
	 */
	private String groupKey;
	/**
	 * 描述/备注
	 */
	private String remark;
	/**
	 * 发件人
	 */
	private String sender;
	/**
	 * 收件人
	 */
	private String receivers;
	/**
	 * 抄送
	 */
	private String cc;
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
	 * 分组名
	 */
	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}
	/**
	 * 分组名
	 */
	public String getGroupKey() {
		return this.groupKey;
	}
	/**
	 * 描述/备注
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 描述/备注
	 */
	public String getRemark() {
		return this.remark;
	}
	/**
	 * 发件人
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}
	/**
	 * 发件人
	 */
	public String getSender() {
		return this.sender;
	}
	/**
	 * 收件人
	 */
	public void setReceivers(String receivers) {
		this.receivers = receivers;
	}
	/**
	 * 收件人
	 */
	public String getReceivers() {
		return this.receivers;
	}
	/**
	 * 抄送
	 */
	public void setCc(String cc) {
		this.cc = cc;
	}
	/**
	 * 抄送
	 */
	public String getCc() {
		return this.cc;
	}
}
