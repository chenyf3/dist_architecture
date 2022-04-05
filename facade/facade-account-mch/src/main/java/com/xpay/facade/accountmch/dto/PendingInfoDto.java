package com.xpay.facade.accountmch.dto;

public class PendingInfoDto {
    private Long pendingId;
    private String accountNo;
    private Integer processType;

    public Long getPendingId() {
        return pendingId;
    }

    public void setPendingId(Long pendingId) {
        this.pendingId = pendingId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public Integer getProcessType() {
        return processType;
    }

    public void setProcessType(Integer processType) {
        this.processType = processType;
    }
}
