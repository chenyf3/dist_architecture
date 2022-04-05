package com.xpay.web.api.common.ddo.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作员操作日志表
 */
public class OperateLogVo implements Serializable {

    private static final long serialVersionUID = 1L;

    //columns START
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime = new Date();

    /**
     * 操作员登录名
     */
    private String loginName;

    /**
     * 商户编号
     */
    private String merchantNo;

    /**
     * 操作类型
     */
    private Integer operateType;

    /**
     * 操作状态（1:成功，-1:失败）
     *
     * @see com.xpay.common.statics.constants.common.PublicStatus
     */
    private Integer status;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作内容
     */
    private String content;

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

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
