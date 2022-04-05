package com.xpay.web.pms.web.vo.devops;

public class MailReceiverVo {
    /**
     * id主键
     */
    private Long id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }
}
