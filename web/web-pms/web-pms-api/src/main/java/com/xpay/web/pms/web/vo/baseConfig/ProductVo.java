package com.xpay.web.pms.web.vo.baseConfig;

import jakarta.validation.constraints.NotNull;

public class ProductVo {
    @NotNull(message = "产品类型不能为空")
    private Integer productType;
    @NotNull(message = "产品编号不能为空")
    private Integer productCode;
    @NotNull(message = "备注不能为空")
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
