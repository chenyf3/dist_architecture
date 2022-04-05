package com.xpay.sdk.api.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING("01"),
    PROCESSING("02"),
    SUCCESS("03"),
    FAIL("04")
    ;

    /** 枚举值 */
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private OrderStatus(String status) {
        this.status = status;
    }
}
