package com.xpay.service.accountmch.entity;

import com.xpay.common.service.annotations.PK;

/**
 * 待账务处理表
 */
public class AccountProcessPending {

	//columns START
	/**
	 * 自增主键
	 */
	@PK
	private Long id;
	/**
	 * 版本号
	 */
	private Integer version;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 创建日期
	 */
	private java.util.Date createDate;
	/**
	 * 修改时间
	 */
	private java.util.Date modifyTime;
	/**
	 * 处理阶段(1=待处理 2=处理中 3=已处理)
	 */
	private Integer processStage;
	/**
	 * 是否可合并批处理(-1=不支持 1=支持)
	 */
	private Integer mergeSupport;
	/**
	 * 账务请求数据(JSON格式)
	 */
	private String requestDto;
	/**
	 * 账务处理数据(JSON格式)
	 */
	private String processDto;
	/**
	 * 备注
	 */
	private String remark;
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
	 * 修改时间
	 */
	public void setModifyTime(java.util.Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	/**
	 * 修改时间
	 */
	public java.util.Date getModifyTime() {
		return this.modifyTime;
	}
	/**
	 * 处理阶段(1=待处理 2=处理中 3=已处理)
	 */
	public void setProcessStage(Integer processStage) {
		this.processStage = processStage;
	}
	/**
	 * 处理阶段(1=待处理 2=处理中 3=已处理)
	 */
	public Integer getProcessStage() {
		return this.processStage;
	}
	/**
	 * 是否可合并批处理(-1=不支持 1=支持)
	 */
	public void setMergeSupport(Integer mergeSupport) {
		this.mergeSupport = mergeSupport;
	}
	/**
	 * 是否可合并批处理(-1=不支持 1=支持)
	 */
	public Integer getMergeSupport() {
		return this.mergeSupport;
	}
	/**
	 * 账务请求数据(JSON格式)
	 */
	public void setRequestDto(String requestDto) {
		this.requestDto = requestDto;
	}
	/**
	 * 账务请求数据(JSON格式)
	 */
	public String getRequestDto() {
		return this.requestDto;
	}
	/**
	 * 账务处理数据(JSON格式)
	 */
	public void setProcessDto(String processDto) {
		this.processDto = processDto;
	}
	/**
	 * 账务处理数据(JSON格式)
	 */
	public String getProcessDto() {
		return this.processDto;
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
