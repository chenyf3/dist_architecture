package com.xpay.facade.accountmch.dto;

public class BussInfoDto {
    private Integer bussType;//业务类型
    private Integer bussCode;//业务编码

    public Integer getBussType() {
        return bussType;
    }

    public void setBussType(Integer bussType) {
        this.bussType = bussType;
    }

    public Integer getBussCode() {
        return bussCode;
    }

    public void setBussCode(Integer bussCode) {
        this.bussCode = bussCode;
    }
}
