package com.xpay.service.extend.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 项目发布记录表
 */
public class PublishRecord {

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
	 * 版本号
	 */
	private Integer version;
	/**
	 * 创建人
	 */
	private String creator;
	/**
	 * 触发的任务名
	 */
	private String jobName;
	/**
	 * 构建流水号
	 */
	private String buildNo;
	/**
	 * 构建说明
	 */
	private String buildMsg;
	/**
	 * 发布项目
	 */
	private String apps;
	/**
	 * 发布的机房
	 */
	private String idc;
	/**
	 * 中继项目
	 */
	private String relayApp;
	/**
	 * 通知邮件
	 */
	private String notifyEmail;
	/**
	 * 回调通知地址
	 */
	private String notifyUrl;
	/**
	 * 发布次数
	 */
	private Integer publishTimes;
	/**
	 * 修改者
	 */
	private String modifier;
	/**
	 * 状态 1=待处理 2=排队中 3=处理中 4=成功 5=失败 6=不稳定 7=已取消 8=已超时
	 */
	private Integer status;
	/**
	 * jenkins队列id
	 */
	private Integer queueId;
	/**
	 * jenkins构建id
	 */
	private Integer buildId;
	/**
	 * 已处理次数
	 */
	private Integer processTimes;
	/**
	 * 备注
	 */
	private String remark;
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
	 * 创建人
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	/**
	 * 创建人
	 */
	public String getCreator() {
		return this.creator;
	}
	/**
	 * 触发的任务名
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	/**
	 * 触发的任务名
	 */
	public String getJobName() {
		return this.jobName;
	}
	/**
	 * 构建流水号
	 */
	public void setBuildNo(String buildNo) {
		this.buildNo = buildNo;
	}
	/**
	 * 构建流水号
	 */
	public String getBuildNo() {
		return this.buildNo;
	}
	/**
	 * 构建说明
	 */
	public void setBuildMsg(String buildMsg) {
		this.buildMsg = buildMsg;
	}
	/**
	 * 构建说明
	 */
	public String getBuildMsg() {
		return this.buildMsg;
	}
	/**
	 * 发布项目
	 */
	public void setApps(String apps) {
		this.apps = apps;
	}
	/**
	 * 发布项目
	 */
	public String getApps() {
		return this.apps;
	}
	/**
	 * 发布的机房
	 */
	public void setIdc(String idc) {
		this.idc = idc;
	}
	/**
	 * 发布的机房
	 */
	public String getIdc() {
		return this.idc;
	}
	/**
	 * 中继项目
	 */
	public void setRelayApp(String relayApp) {
		this.relayApp = relayApp;
	}
	/**
	 * 中继项目
	 */
	public String getRelayApp() {
		return this.relayApp;
	}
	/**
	 * 通知邮件
	 */
	public void setNotifyEmail(String notifyEmail) {
		this.notifyEmail = notifyEmail;
	}
	/**
	 * 通知邮件
	 */
	public String getNotifyEmail() {
		return this.notifyEmail;
	}
	/**
	 * 回调通知地址
	 */
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	/**
	 * 回调通知地址
	 */
	public String getNotifyUrl() {
		return this.notifyUrl;
	}
	/**
	 * 发布次数
	 */
	public void setPublishTimes(Integer publishTimes) {
		this.publishTimes = publishTimes;
	}
	/**
	 * 发布次数
	 */
	public Integer getPublishTimes() {
		return this.publishTimes;
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
	/**
	 * 状态 1=待处理 2=排队中 3=处理中 4=成功 5=失败 6=不稳定 7=已取消 8=已超时
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 状态 1=待处理 2=排队中 3=处理中 4=成功 5=失败 6=不稳定 7=已取消 8=已超时
	 */
	public Integer getStatus() {
		return this.status;
	}
	/**
	 * jenkins队列id
	 */
	public void setQueueId(Integer queueId) {
		this.queueId = queueId;
	}
	/**
	 * jenkins队列id
	 */
	public Integer getQueueId() {
		return this.queueId;
	}
	/**
	 * jenkins构建id
	 */
	public void setBuildId(Integer buildId) {
		this.buildId = buildId;
	}
	/**
	 * jenkins构建id
	 */
	public Integer getBuildId() {
		return this.buildId;
	}
	/**
	 * 已处理次数
	 */
	public void setProcessTimes(Integer processTimes) {
		this.processTimes = processTimes;
	}
	/**
	 * 已处理次数
	 */
	public Integer getProcessTimes() {
		return this.processTimes;
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

}
