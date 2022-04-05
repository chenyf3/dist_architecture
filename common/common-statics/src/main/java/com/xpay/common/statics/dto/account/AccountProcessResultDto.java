package com.xpay.common.statics.dto.account;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;

import java.util.List;

/**
 * 账务处理结果回调DTO
 */
public class AccountProcessResultDto extends MsgDto {
    /**
     * 账务处理结果 1=成功 -1=失败
     */
    private Integer processResult;

    /**
     * 错误码
     */
    private Integer errCode;

    /**
     * 错误描述
     */
    private String errMsg;

    /**
     * 账务处理请求相关信息，跟请求账务处理时DTO的顺序一致
     */
    private List<RequestInfo> requestInfos;


    public Integer getProcessResult() {
        return processResult;
    }

    public void setProcessResult(Integer processResult) {
        this.processResult = processResult;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public List<RequestInfo> getRequestInfos() {
        return requestInfos;
    }

    public void setRequestInfos(List<RequestInfo> requestInfos) {
        this.requestInfos = requestInfos;
    }

    public static class RequestInfo {
        private String accountNo;//账户编号
        private String trxNo;//交易流水号
        private Integer processType;//处理类型
        private String accountTime;//记账时间，格式：yyyy-MM-dd HH:mm:ss，如果账务处理失败则为NULL

        public String getAccountNo() {
            return accountNo;
        }

        public void setAccountNo(String accountNo) {
            this.accountNo = accountNo;
        }

        public String getTrxNo() {
            return trxNo;
        }

        public void setTrxNo(String trxNo) {
            this.trxNo = trxNo;
        }

        public Integer getProcessType() {
            return processType;
        }

        public void setProcessType(Integer processType) {
            this.processType = processType;
        }

        public String getAccountTime() {
            return accountTime;
        }

        public void setAccountTime(String accountTime) {
            this.accountTime = accountTime;
        }
    }
}
