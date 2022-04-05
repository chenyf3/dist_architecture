package com.xpay.demo.es.order;

/**
 * 模拟商户表
 */
public class Merchant {
    private String mchNo;//商户编号
    private String name;//商户名称

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
