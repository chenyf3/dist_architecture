package com.xpay.facade.message.params;

public class SmsQueryParam {
    private Integer platform;//短信运营平台
    private String phone;//手机号，必选
    private String sendDate;//发送日期，必选，格式：yyyy-MM-dd
    private Integer pageSize;//页大小，MAX=50，必选
    private Integer currentPage;//当前页，必选
    private String serialNo;//发送流水号，可选

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
}
