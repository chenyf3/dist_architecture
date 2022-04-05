package com.xpay.common.statics.dto.accountmch;

import java.util.List;

/**
 * 账务处理结果回调DTO
 */
public class AccountMchProcessResultDto {
    /**
     * 账务处理流水号，即请求账务处理时的账务处理流水号
     */
    private String accountProcessNo;

    /**
     * 账务处理结果 100=成功 101=失败
     */
    private Integer processResult;

    /**
     * 交易流水号，跟请求账务处理时DTO的顺序一致
     */
    private List<String> trxNos;

    /**
     * 用户号，跟请求账务处理时DTO的顺序一致
     */
    private List<String> userNos;

    /**
     * 记账时间，跟请求账务处理时DTO的顺序一致
     */
    private List<String> accountTimes;

    /**
     * 账务处理类型，跟请求账务处理时DTO的顺序一致
     *
     * @see com.xpay.common.statics.enums.account.AccountProcessTypeEnum
     */
    private List<Integer> processTypes;

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误描述
     */
    private String errorMsg;

    /**
     * 额外的参数，账务回调时可以额外带上这些参数
     */
    private String extraMsg;

    public String getAccountProcessNo() {
        return accountProcessNo;
    }

    public void setAccountProcessNo(String accountProcessNo) {
        this.accountProcessNo = accountProcessNo;
    }

    public Integer getProcessResult() {
        return processResult;
    }

    public void setProcessResult(Integer processResult) {
        this.processResult = processResult;
    }

    public List<String> getTrxNos() {
        return trxNos;
    }

    public void setTrxNos(List<String> trxNos) {
        this.trxNos = trxNos;
    }

    public List<String> getUserNos() {
        return userNos;
    }

    public void setUserNos(List<String> userNos) {
        this.userNos = userNos;
    }

    public List<Integer> getProcessTypes() {
        return processTypes;
    }

    public void setProcessTypes(List<Integer> processTypes) {
        this.processTypes = processTypes;
    }

    public List<String> getAccountTimes() {
        return accountTimes;
    }

    public void setAccountTimes(List<String> accountTimes) {
        this.accountTimes = accountTimes;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getExtraMsg() {
        return extraMsg;
    }

    public void setExtraMsg(String extraMsg) {
        this.extraMsg = extraMsg;
    }
}
