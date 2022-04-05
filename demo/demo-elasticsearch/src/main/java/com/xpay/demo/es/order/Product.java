package com.xpay.demo.es.order;

import java.math.BigDecimal;

/**
 * 模拟商品表
 */
public class Product {
    private Long id;//商品id
    private String name;//商品名称
    private BigDecimal price;//商品价格
    private Integer category;//商品类目
    private String mchNo;//商家编号

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }
}
