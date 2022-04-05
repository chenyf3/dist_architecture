/*
 * Powered By [xpay.com]
 */
package com.xpay.service.timer.entity;

/**
 * 操作日志表
 */
public class OpLog {

	//columns START
	/**
	 * 自增主键
	 */
	private Long id;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 操作主体
	 */
	private String objKey;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 内容描述
	 */
	private Object content;
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
	 * 操作主体
	 */
	public void setObjKey(String objKey) {
		this.objKey = objKey;
	}
	/**
	 * 操作主体
	 */
	public String getObjKey() {
		return this.objKey;
	}
	/**
	 * 备注
	 */
	public String getRemark() {
		return remark;
	}
	/**
	 * 备注
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 内容描述
	 */
	public void setContent(Object content) {
		this.content = content;
	}
	/**
	 * 内容描述
	 */
	public Object getContent() {
		return this.content;
	}
}
