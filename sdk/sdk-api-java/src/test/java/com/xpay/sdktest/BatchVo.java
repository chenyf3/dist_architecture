package com.xpay.sdktest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class BatchVo implements Serializable {
    private String totalCount;
    private BigDecimal totalAmount;
    private List<SingleVo> detailList;

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SingleVo> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<SingleVo> detailList) {
        this.detailList = detailList;
    }
}
