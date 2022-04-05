package com.xpay.service.message.entity;

import com.xpay.common.service.annotations.PK;

import java.util.Date;

/**
 * activemq消息轨迹表
 */
public class AmqTrace {

	//columns START
	/**
	 * 自增主键
	 */
	@PK
	private Long id;
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 消息发送/消费的时间
	 */
	private Date msgTime;

	/**
	 * 轨迹追踪ID
	 */
	private String traceId;

	/**
	 * 业务流水号
	 */
	private String trxNo;

	/**
	 * 商户编号
	 */
	private String mchNo;

	/**
	 * 队列名
	 */
	private String topic;

	/**
	 * 队列分组
	 */
	private String topicGroup;

	/**
	 * 消费端的队列名
	 */
	private String consumeDest;

	/**
	 * 消息类型(1=发送 2=消费)
	 */
	private Integer type;

	/**
	 * 消息状态(1=发送/消费成功 2=发送/消费失败)
	 */
	private Integer msgStatus;

	/**
	 * 投递次数(生产者恒为1)
	 */
	private Integer deliveryCount;

	/**
	 * 客户端标识
	 */
	private String clientFlag;

	/**
	 * 是否补发的消息(1=是 2=否)
	 */
	private Integer resend;

	/**
	 * 错误信息
	 */
	private String errMsg;

	/**
	 * 源消息体
	 */
	private String oriMsg;

	//columns END


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 消息发送/消费的时间
	 */
	public void setMsgTime(Date msgTime) {
		this.msgTime = msgTime;
	}
	/**
	 * 消息发送/消费的时间
	 */
	public Date getMsgTime() {
		return this.msgTime;
	}
	/**
	 * 轨迹追踪ID
	 */
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	/**
	 * 轨迹追踪ID
	 */
	public String getTraceId() {
		return this.traceId;
	}
	/**
	 * 业务流水号
	 */
	public void setTrxNo(String trxNo) {
		this.trxNo = trxNo;
	}
	/**
	 * 业务流水号
	 */
	public String getTrxNo() {
		return this.trxNo;
	}
	/**
	 * 商户编号
	 */
	public String getMchNo() {
		return mchNo;
	}
	/**
	 * 商户编号
	 */
	public void setMchNo(String mchNo) {
		this.mchNo = mchNo;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getConsumeDest() {
		return consumeDest;
	}

	public void setConsumeDest(String consumeDest) {
		this.consumeDest = consumeDest;
	}

	public String getTopicGroup() {
		return topicGroup;
	}

	public void setTopicGroup(String topicGroup) {
		this.topicGroup = topicGroup;
	}

	/**
	 * 消息类型(1=发送 2=消费)
	 */
	public void setType(Integer type) {
		this.type = type;
	}
	/**
	 * 消息类型(1=发送 2=消费)
	 */
	public Integer getType() {
		return this.type;
	}
	/**
	 * 消息状态(1=发送/消费成功 2=发送/消费失败)
	 */
	public void setMsgStatus(Integer msgStatus) {
		this.msgStatus = msgStatus;
	}
	/**
	 * 消息状态(1=发送/消费成功 2=发送/消费失败)
	 */
	public Integer getMsgStatus() {
		return this.msgStatus;
	}
	/**
	 * 投递次数(生产者恒为1)
	 */
	public void setDeliveryCount(Integer deliveryCount) {
		this.deliveryCount = deliveryCount;
	}
	/**
	 * 投递次数(生产者恒为1)
	 */
	public Integer getDeliveryCount() {
		return this.deliveryCount;
	}
	/**
	 * 客户端标识
	 */
	public void setClientFlag(String clientFlag) {
		this.clientFlag = clientFlag;
	}
	/**
	 * 客户端标识
	 */
	public String getClientFlag() {
		return this.clientFlag;
	}
	/**
	 * 是否补发的消息(1=是 2=否)
	 */
	public Integer getResend() {
		return resend;
	}
	/**
	 * 是否补发的消息(1=是 2=否)
	 */
	public void setResend(Integer resend) {
		this.resend = resend;
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

}
