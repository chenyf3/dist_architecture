package com.xpay.service.timer.entity;

import java.io.Serializable;

/**
 * 运行实例表
 */
public class Instance implements Serializable {

	//columns START
	/**
	 * 自增ID
	 */
	private Long id;
	/**
	 * 实例ID
	 */
	private String instanceId;
	/**
	 * 域名
	 */
	private String host;
	/**
	 * IP地址
	 */
	private String ip;
	/**
	 * 实例状态(1=已启动 2=已关闭)
	 */
	private Integer status;
	/**
	 * 调度状态(1=运行中 2=挂起中)
	 */
	private Integer scheduleStatus;
	/**
	 * 通信地址(host:port)
	 */
	private String url;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 更新时间
	 */
	private java.util.Date updateTime;
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
	 * 实例ID
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	/**
	 * 实例ID
	 */
	public String getInstanceId() {
		return this.instanceId;
	}
	/**
	 * 域名
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * 域名
	 */
	public String getHost() {
		return this.host;
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
	 * 实例状态(1=已启动 2=已关闭)
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 实例状态(1=已启动 2=已关闭)
	 */
	public Integer getStatus() {
		return this.status;
	}
	/**
	 * 调度状态(1=运行中 2=挂起中)
	 */
	public void setScheduleStatus(Integer scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}
	/**
	 * 调度状态(1=运行中 2=挂起中)
	 */
	public Integer getScheduleStatus() {
		return this.scheduleStatus;
	}
	/**
	 * 通信地址(host:port)
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 通信地址(host:port)
	 */
	public String getUrl() {
		return this.url;
	}
	/**
	 * 备注
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 备注
	 */
	public String getRemark() {
		return this.remark;
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
	 * 更新时间
	 */
	public void setUpdateTime(java.util.Date updateTime) {
		this.updateTime = updateTime;
	}
	/**
	 * 更新时间
	 */
	public java.util.Date getUpdateTime() {
		return this.updateTime;
	}
}
