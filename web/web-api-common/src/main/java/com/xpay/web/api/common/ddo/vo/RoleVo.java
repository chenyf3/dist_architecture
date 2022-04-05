package com.xpay.web.api.common.ddo.vo;

import java.io.Serializable;
import java.util.Date;

public class RoleVo implements Serializable {
    private Long id;
    private Integer version = 0;
    private Date createTime = new Date();
    private String roleName; // 角色名称
    private String remark; // 描述
    private Integer roleType;
    private String mchNo;
    private Integer mchType;
    private Integer autoAssign;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public Integer getMchType() {
        return mchType;
    }

    public void setMchType(Integer mchType) {
        this.mchType = mchType;
    }

    public Integer getAutoAssign() {
        return autoAssign;
    }

    public void setAutoAssign(Integer autoAssign) {
        this.autoAssign = autoAssign;
    }
}
