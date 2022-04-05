package com.xpay.common.api.exceptions;

import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.statics.exception.BizException;

public class ApiException extends BizException {
    private String respCode;
    private String respMsg;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    /**
     * 参数校验失败时抛出的异常
     * @param errCode
     * @param errMsg
     * @return
     */
    public static ApiException paramValidFail(String errCode, String errMsg){
        ApiException ex = new ApiException();
        ex.setRespCode(ApiRespCodeEnum.PARAM_FAIL.getValue());
        ex.setRespMsg(errMsg + "[" + errCode + "]");
        return ex;
    }

    /**
     * 业务规则校验失败时抛出的异常
     * @param errCode
     * @param errMsg
     * @return
     */
    public static ApiException bizValidFail(String errCode, String errMsg){
        ApiException ex = new ApiException();
        ex.setRespCode(ApiRespCodeEnum.BIZ_FAIL.getValue());
        ex.setRespMsg(errMsg + "[" + errCode + "]");
        return ex;
    }
}
