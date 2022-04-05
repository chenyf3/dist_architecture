package com.xpay.common.api.dto;

/**
 * 异步回调时商户侧的响应对象
 */
public class CallbackResp {
    public final static int TIMEOUT_HTTP_STATUS = -1;
    public final static String RESP_SUCCESS = "01";//成功
    public final static String RESP_RETRY = "02";//重试，当http调用失败时，需要重试

    public final static String VERIFY_NONE = "0";
    public final static String VERIFY_PASS = "1";
    public final static String VERIFY_FAIL = "2";
    public final static String VERIFY_ERR = "-1";

    /**
     * 网络请求的http状态码
     */
    private int httpStatus;
    /**
     * 网络请求异常的描述
     */
    private String httpError;
    /**
     * 响应体
     */
    private byte[] body;
    /**
     * 商户响应的code
     */
    private String code;
    /**
     * 响应签名串
     */
    private String signature;
    /**
     * 签名类型
     */
    private String signType;
    /**
     * 验签结果
     */
    private String verifyResult = VERIFY_NONE;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getHttpError() {
        return httpError;
    }

    public void setHttpError(String httpError) {
        this.httpError = httpError;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(String verifyResult) {
        this.verifyResult = verifyResult;
    }

    /**
     * 是否需要重试，以下情况允许重试
     *  1、网络请求成功，并且用户响应数据中要求重试的
     *  2、网络请求失败，并且是属于超时的情况
     *  3、需要验签并且验签失败
     * @return
     */
    public boolean isNeedRetry(){
        return RESP_RETRY.equals(code)
                || (httpStatus == TIMEOUT_HTTP_STATUS)
                || (VERIFY_FAIL.equals(verifyResult) || VERIFY_ERR.equals(verifyResult));
    }

    /**
     * 网络请求是否成功
     * @return
     */
    public boolean isRequestOk() {
        return httpStatus >= 200 && httpStatus < 300;
    }

    /**
     * 商户侧是否响应为通知成功
     * @return
     */
    public boolean isRespSuccess(){
        return RESP_SUCCESS.equals(code);
    }
}
