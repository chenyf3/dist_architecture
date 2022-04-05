package com.xpay.sdktest;

import java.util.List;

public class BatchRespVo {
    private String orderStatus;
    private String totalCount;
    private List<SingleRespVo> singleList;

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public List<SingleRespVo> getSingleList() {
        return singleList;
    }

    public void setSingleList(List<SingleRespVo> singleList) {
        this.singleList = singleList;
    }
}
