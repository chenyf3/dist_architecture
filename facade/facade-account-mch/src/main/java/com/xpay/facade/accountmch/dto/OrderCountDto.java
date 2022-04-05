package com.xpay.facade.accountmch.dto;

public class OrderCountDto {
    private Integer totalReceiveCount = 0;
    private Integer totalDebitCount = 0;
    private Integer totalReturnCount = 0;

    public Integer getTotalReceiveCount() {
        return totalReceiveCount;
    }

    public void setTotalReceiveCount(Integer totalReceiveCount) {
        this.totalReceiveCount = totalReceiveCount;
    }

    public Integer getTotalDebitCount() {
        return totalDebitCount;
    }

    public void setTotalDebitCount(Integer totalDebitCount) {
        this.totalDebitCount = totalDebitCount;
    }

    public Integer getTotalReturnCount() {
        return totalReturnCount;
    }

    public void setTotalReturnCount(Integer totalReturnCount) {
        this.totalReturnCount = totalReturnCount;
    }

    public static OrderCountDto fromAccountOrderCount(String totalOrderCount){
        OrderCountDto countDto = new OrderCountDto();
        String[] countArr = totalOrderCount.split(",");
        countDto.setTotalReceiveCount(Integer.valueOf(countArr[0]));
        countDto.setTotalDebitCount(Integer.valueOf(countArr[1]));
        countDto.setTotalReturnCount(Integer.valueOf(countArr[2]));
        return countDto;
    }
}
