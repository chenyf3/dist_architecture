package com.xpay.web.api.common.ddo.dto;

public class RoleQueryDto {
    private Integer currentPage;
    private Integer pageSize;
    private String roleName = null;
    private Integer roleType = null;
    private String mchNo = null;
    private Integer mchType = null;

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
}
