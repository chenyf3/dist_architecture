package com.xpay.service.accountmch.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 账务处理结果表
 */
public class AccountProcessResult implements Serializable {

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
	 * 处理结果(1=成功 -1=失败)
	 */
	private Integer processResult;
	/**
	 * 错误码(0=无异常 1=系统异常 其他=具体业务异常)
	 */
	private Integer errCode;
	/**
	 * 错误信息
	 */
	private String errMsg;
	/**
	 * 回调阶段(1=待审核 2=待回调 3=已回调 4=不回调)
	 */
	private Integer callbackStage;
	/**
	 * 是否来自异步账务处理(1=是 -1=否)
	 */
	private Integer isFromAsync;
	/**
	 * 账务请求数据(JSON格式)
	 */
	private String requestDto;
	/**
	 * 账务处理的数据(JSON格式)
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
	 * 处理结果(1=成功 -1=失败)
	 */
	public void setProcessResult(Integer processResult) {
		this.processResult = processResult;
	}
	/**
	 * 处理结果(1=成功 -1=失败)
	 */
	public Integer getProcessResult() {
		return this.processResult;
	}
	/**
	 * 错误码(0=无异常 1=系统异常 其他=具体业务异常)
	 */
	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
	}
	/**
	 * 错误码(0=无异常 1=系统异常 其他=具体业务异常)
	 */
	public Integer getErrCode() {
		return this.errCode;
	}
	/**
	 * 错误信息
	 */
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	/**
	 * 错误信息
	 */
	public String getErrMsg() {
		return this.errMsg;
	}
	/**
	 * 回调阶段(1=待审核 2=待回调 3=已回调 4=不回调)
	 */
	public void setCallbackStage(Integer callbackStage) {
		this.callbackStage = callbackStage;
	}
	/**
	 * 回调阶段(1=待审核 2=待回调 3=已回调 4=不回调)
	 */
	public Integer getCallbackStage() {
		return this.callbackStage;
	}
	/**
	 * 是否来自异步账务处理(1=是 -1=否)
	 */
	public void setIsFromAsync(Integer isFromAsync) {
		this.isFromAsync = isFromAsync;
	}
	/**
	 * 是否来自异步账务处理(1=是 -1=否)
	 */
	public Integer getIsFromAsync() {
		return this.isFromAsync;
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
	 * 账务处理的数据(JSON格式)
	 */
	public void setProcessDto(String processDto) {
		this.processDto = processDto;
	}
	/**
	 * 账务处理的数据(JSON格式)
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
