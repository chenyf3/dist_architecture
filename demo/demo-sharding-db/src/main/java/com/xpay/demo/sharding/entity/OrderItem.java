package com.xpay.demo.sharding.entity;

import com.xpay.common.service.annotations.PK;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItem implements Serializable {
    @PK
    private Long orderId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer count;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
