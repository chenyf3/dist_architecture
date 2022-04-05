package com.xpay.demo.es.order;

/**
 * 模拟用户表
 */
public class User {
    private Long id;//用户id
    private String expressAddress;//用户物流地址

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExpressAddress() {
        return expressAddress;
    }

    public void setExpressAddress(String expressAddress) {
        this.expressAddress = expressAddress;
    }
}
