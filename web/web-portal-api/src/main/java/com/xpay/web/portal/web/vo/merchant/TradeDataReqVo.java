package com.xpay.web.portal.web.vo.merchant;

import jakarta.validation.constraints.NotEmpty;

public class TradeDataReqVo {
    @NotEmpty(message="开始日期时间不能为空")
    private String startDay;
    @NotEmpty(message="结束日期时间不能为空")
    private String endDay;

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }
}
