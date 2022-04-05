package com.xpay.sdk.api.entity;

import com.xpay.sdk.api.enums.RespCode;

/**
 * 响应给商户的VO
 */
public class Response {
    private String respCode;
    private String respMsg;
    private Object data;
    private String mchNo;
    private String randStr;
    private String signType;
    private String secKey;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
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

    /**
     * 判断本次请求是否成功
     * @return
     */
    public boolean isSuccess(){
        return RespCode.SUCCESS.getCode().equals(this.respCode);
    }

    /**
     * 获取 resp_msg 中的错误码，如：resp_msg的值为：[200006]-参数为空，则提取出的错误码为 200006
     * @return
     */
    public String respMsgCode(){
        if(respMsg != null && (respMsg.startsWith("[") && respMsg.indexOf("]-") > 0)){
            return respMsg.substring(1, respMsg.indexOf("]-"));
        }else{
            return null;
        }
    }

    public String[] splitSecKey(){
        if(this.secKey == null){
            return null;
        }
        return secKey.split(":");
    }
}
