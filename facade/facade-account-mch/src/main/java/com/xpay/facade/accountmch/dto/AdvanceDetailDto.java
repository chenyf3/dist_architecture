package com.xpay.facade.accountmch.dto;

import java.math.BigDecimal;

public class AdvanceDetailDto {
    /**
     * 垫资比例
     */
    private java.math.BigDecimal advanceRatio;

    /**
     * 最大垫资金额
     */
    private java.math.BigDecimal maxAvailAdvanceAmount;

    /**
     * 累计收单总额
     */
    private java.math.BigDecimal totalCreditAmount;

    /**
     * 累计扣款总额
     */
    private java.math.BigDecimal totalDebitAmount;

    /**
     * 累计退回总额
     */
    private java.math.BigDecimal totalReturnAmount;

    /**
     * 累计可用垫资扣款总额
     */
    private java.math.BigDecimal totalAdvanceDebitAmount;

    /**
     * 累计可用垫资退回总额
     */
    private java.math.BigDecimal totalAdvanceReturnAmount;

    /**
     * 跨日退回总额
     */
    private java.math.BigDecimal totalCrossReturnAmount;

    /**
     * 累计订单总数(收单,出款,退回)
     */
    private java.lang.String totalOrderCount;


    public BigDecimal getAdvanceRatio() {
        return advanceRatio;
    }

    public void setAdvanceRatio(BigDecimal advanceRatio) {
        this.advanceRatio = advanceRatio;
    }

    public BigDecimal getMaxAvailAdvanceAmount() {
        return maxAvailAdvanceAmount;
    }

    public void setMaxAvailAdvanceAmount(BigDecimal maxAvailAdvanceAmount) {
        this.maxAvailAdvanceAmount = maxAvailAdvanceAmount;
    }

    public BigDecimal getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public void setTotalCreditAmount(BigDecimal totalCreditAmount) {
        this.totalCreditAmount = totalCreditAmount;
    }

    public BigDecimal getTotalDebitAmount() {
        return totalDebitAmount;
    }

    public void setTotalDebitAmount(BigDecimal totalDebitAmount) {
        this.totalDebitAmount = totalDebitAmount;
    }

    public BigDecimal getTotalReturnAmount() {
        return totalReturnAmount;
    }

    public void setTotalReturnAmount(BigDecimal totalReturnAmount) {
        this.totalReturnAmount = totalReturnAmount;
    }

    public BigDecimal getTotalAdvanceDebitAmount() {
        return totalAdvanceDebitAmount;
    }

    public void setTotalAdvanceDebitAmount(BigDecimal totalAdvanceDebitAmount) {
        this.totalAdvanceDebitAmount = totalAdvanceDebitAmount;
    }

    public BigDecimal getTotalAdvanceReturnAmount() {
        return totalAdvanceReturnAmount;
    }

    public void setTotalAdvanceReturnAmount(BigDecimal totalAdvanceReturnAmount) {
        this.totalAdvanceReturnAmount = totalAdvanceReturnAmount;
    }

    public BigDecimal getTotalCrossReturnAmount() {
        return totalCrossReturnAmount;
    }

    public void setTotalCrossReturnAmount(BigDecimal totalCrossReturnAmount) {
        this.totalCrossReturnAmount = totalCrossReturnAmount;
    }

    public String getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(String totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }
}
