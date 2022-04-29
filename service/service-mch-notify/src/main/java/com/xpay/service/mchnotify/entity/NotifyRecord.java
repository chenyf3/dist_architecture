/*
 * Powered By [xpay.com]
 */
package com.xpay.service.mchnotify.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;

/**
 * 商户通知表
 */
public class NotifyRecord implements Serializable {

	//columns START
	/**
	 * ID
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
	 * 商户编号
	 */
	private String mchNo;
	/**
	 * 系统流水号
	 */
	private String trxNo;
	/**
	 * 商户流水号
	 */
	private String mchTrxNo;
	/**
	 * 回调地址
	 */
	private String url;
	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 通知次数
	 */
	private Integer currTimes;
	/**
	 * 产品类型
	 */
	private Integer productType;
	/**
	 * 产品编码
	 */
	private Integer productCode;
	/**
	 * 源消息体
	 */
	private String oriMsg;
	/**
	 * 通知日志
	 */
	private String notifyLogs;
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
	 * 系统流水号
	 */
	public void setTrxNo(String trxNo) {
		this.trxNo = trxNo;
	}
	/**
	 * 系统流水号
	 */
	public String getTrxNo() {
		return this.trxNo;
	}
	/**
	 * 商户流水号
	 */
	public String getMchTrxNo() {
		return mchTrxNo;
	}
	/**
	 * 商户流水号
	 */
	public void setMchTrxNo(String mchTrxNo) {
		this.mchTrxNo = mchTrxNo;
	}
	/**
	 * 回调地址
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * 回调地址
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * 通知次数
	 */
	public void setCurrTimes(Integer currTimes) {
		this.currTimes = currTimes;
	}
	/**
	 * 通知次数
	 */
	public Integer getCurrTimes() {
		return this.currTimes;
	}
	/**
	 * 产品类型
	 */
	public void setProductType(Integer productType) {
		this.productType = productType;
	}
	/**
	 * 产品类型
	 */
	public Integer getProductType() {
		return this.productType;
	}
	/**
	 * 产品编码
	 */
	public void setProductCode(Integer productCode) {
		this.productCode = productCode;
	}
	/**
	 * 产品编码
	 */
	public Integer getProductCode() {
		return this.productCode;
	}
	/**
	 * 源消息体
	 */
	public void setOriMsg(String oriMsg) {
		this.oriMsg = oriMsg;
	}

	/**
	 * 源消息体
	 */
	public String getOriMsg() {
		return this.oriMsg;
	}

	public String getNotifyLogs() {
		return notifyLogs;
	}

	public void setNotifyLogs(String notifyLogs) {
		this.notifyLogs = notifyLogs;
	}
}
