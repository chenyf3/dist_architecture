package com.xpay.common.api.params;

/**
 * 异步回调的数据传输对象
 */
public class CallbackParam<T> {
    public final static String SIGN_RESP_VALUE = "01";

    private String mchNo;
    private T data;
    private String randStr;
    private String signType;
    private String secKey;
    private String signResp;//响应体是否需要进行签名校验

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public String getSignResp() {
        return signResp;
    }

    public void setSignResp(String signResp) {
        this.signResp = signResp;
    }
}
