package com.xpay.web.pms.web.vo.merchant;

import jakarta.validation.constraints.NotNull;

public class MerchantQueryVo {
    @NotNull(message = "当前页不能为空")
    private Integer currentPage;
    @NotNull(message = "分页条数不能为空")
    private Integer pageSize;
    private String mchNo;
    private Integer mchType;
    private String fullName;
    private Integer status;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
