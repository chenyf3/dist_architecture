package com.xpay.web.pms.web.vo.merchant;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class MerchantInfoVo {
    @NotNull(message = "商户类型不能为空")
    private Integer mchType;//商户类型
    @NotEmpty(message = "商户全称不能为空")
    private String fullName;//全称
    @NotEmpty(message = "商户简称不能为空")
    private String shortName;//简称
    private String address;//地址
    private String telephone;//电话
    @NotEmpty(message = "业务联系手机不能为空")
    private String bussMobileNo;//业务联系手机
    @NotEmpty(message = "业务联系邮箱不能为空")
    private String bussContactEmail;//业务联系邮箱
    private String url;//网址

    public Integer getMchType() {
        return mchType;
    }

    public void setMchType(Integer mchType) {
        this.mchType = mchType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBussMobileNo() {
        return bussMobileNo;
    }

    public void setBussMobileNo(String bussMobileNo) {
        this.bussMobileNo = bussMobileNo;
    }

    public String getBussContactEmail() {
        return bussContactEmail;
    }

    public void setBussContactEmail(String bussContactEmail) {
        this.bussContactEmail = bussContactEmail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
