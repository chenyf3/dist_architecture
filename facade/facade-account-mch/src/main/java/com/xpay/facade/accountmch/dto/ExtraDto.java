package com.xpay.facade.accountmch.dto;

public class ExtraDto {
    private AdvanceDetailDto advanceDetail;
    private BussInfoDto bussInfo;

    public AdvanceDetailDto getAdvanceDetail() {
        return advanceDetail;
    }

    public void setAdvanceDetail(AdvanceDetailDto advanceDetail) {
        this.advanceDetail = advanceDetail;
    }

    public BussInfoDto getBussInfo() {
        return bussInfo;
    }

    public void setBussInfo(BussInfoDto bussInfo) {
        this.bussInfo = bussInfo;
    }
}
