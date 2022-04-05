package com.xpay.web.pms.web.vo.merchant;

import jakarta.validation.constraints.NotNull;

public class MchNotifyQueryVo {
    @NotNull(message = "当前页不能为空")
    private Integer currentPage;
    @NotNull(message = "分页条数不能为空")
    private Integer pageSize;
    private String createTimeBegin;
    private String createTimeEnd;
    private String mchNo;
    private String trxNo;
    private String mchTrxNo;
    private Integer status;
    private Integer productType;
    private Integer productCode;

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

    public String getCreateTimeBegin() {
        return createTimeBegin;
    }

    public void setCreateTimeBegin(String createTimeBegin) {
        this.createTimeBegin = createTimeBegin;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }

    public String getMchTrxNo() {
        return mchTrxNo;
    }

    public void setMchTrxNo(String mchTrxNo) {
        this.mchTrxNo = mchTrxNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }
}
