package com.xpay.service.message.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 邮件延迟发送
 */
public class MailDelayRecord {
	//columns START
	/**
	 * 自增ID
	 */
	@PK
	private Long id;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 创建日期
	 */
	private java.util.Date createDate;
	/**
	 * 版本号
	 */
	private Integer version;
	/**
	 * 分组名
	 */
	private String groupKey;
	/**
	 * 邮件主题
	 */
	private String subject;
	/**
	 * 邮件内容
	 */
	private String content;
	/**
	 * 流水号
	 */
	private String trxNo;
	/**
	 * 状态(1=待发送 2=发送中 3=已发送)
	 */
	private Integer status;
	/**
	 * 发送开始时间
	 */
	private java.util.Date sendStartTime;
	/**
	 * 发送完成时间
	 */
	private java.util.Date sendFinishTime;
	//columns END


	/**
	 * 自增ID
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 自增ID
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
	 * 创建日期
	 */
	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}
	/**
	 * 创建日期
	 */
	public java.util.Date getCreateDate() {
		return this.createDate;
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
	 * 邮件主题
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * 邮件主题
	 */
	public String getSubject() {
		return this.subject;
	}
	/**
	 * 邮件内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 邮件内容
	 */
	public String getContent() {
		return this.content;
	}
	/**
	 * 流水号
	 */
	public void setTrxNo(String trxNo) {
		this.trxNo = trxNo;
	}
	/**
	 * 流水号
	 */
	public String getTrxNo() {
		return this.trxNo;
	}
	/**
	 * 状态(1=待发送 2=发送中 3=已发送)
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 状态(1=待发送 2=发送中 3=已发送)
	 */
	public Integer getStatus() {
		return this.status;
	}
	/**
	 * 发送开始时间
	 */
	public void setSendStartTime(java.util.Date sendStartTime) {
		this.sendStartTime = sendStartTime;
	}
	/**
	 * 发送开始时间
	 */
	public java.util.Date getSendStartTime() {
		return this.sendStartTime;
	}
	/**
	 * 发送完成时间
	 */
	public void setSendFinishTime(java.util.Date sendFinishTime) {
		this.sendFinishTime = sendFinishTime;
	}
	/**
	 * 发送完成时间
	 */
	public java.util.Date getSendFinishTime() {
		return this.sendFinishTime;
	}
}
