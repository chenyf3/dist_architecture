package com.xpay.demo.backend.mvc.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class SingleVo implements Serializable {
    private String productName;
    private BigDecimal productAmount;
    private String count;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(BigDecimal productAmount) {
        this.productAmount = productAmount;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
