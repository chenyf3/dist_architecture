package com.xpay.demo.sharding.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;
import java.math.BigDecimal;

public class Order implements Serializable {
    @PK
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String remark;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
