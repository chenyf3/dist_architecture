package com.xpay.web.pms.web.vo.baseConfig;

import jakarta.validation.constraints.NotNull;

public class ProductQueryVo {
    @NotNull(message = "当前页不能为空")
    private Integer currentPage;
    @NotNull(message = "分页条数不能为空")
    private Integer pageSize;
    private Integer productType;
    private Integer productCode;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
