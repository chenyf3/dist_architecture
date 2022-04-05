package com.xpay.web.pms.web.vo.merchant;

public class ResetTradePwdInfoVo {
    private String mchNo;
    private String mchName;
    private String adminLoginName;
    private String adminRealName;
    private String adminMobileNo;

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getMchName() {
        return mchName;
    }

    public void setMchName(String mchName) {
        this.mchName = mchName;
    }

    public String getAdminLoginName() {
        return adminLoginName;
    }

    public void setAdminLoginName(String adminLoginName) {
        this.adminLoginName = adminLoginName;
    }

    public String getAdminRealName() {
        return adminRealName;
    }

    public void setAdminRealName(String adminRealName) {
        this.adminRealName = adminRealName;
    }

    public String getAdminMobileNo() {
        return adminMobileNo;
    }

    public void setAdminMobileNo(String adminMobileNo) {
        this.adminMobileNo = adminMobileNo;
    }
}
