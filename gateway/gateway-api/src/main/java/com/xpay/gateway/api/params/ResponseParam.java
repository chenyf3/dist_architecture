package com.xpay.gateway.api.params;

import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;

/**
 * 响应用户请求的参数
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseParam {
    private String respCode;
    private String respMsg;
    private String mchNo;
    private Object data;
    private String randStr;
    private String signType;
    private String secKey;

    public static ResponseParam success(String mchNo){
        ResponseParam responseParam = new ResponseParam();
        responseParam.setRespCode(ApiRespCodeEnum.SUCCESS.getValue());
        responseParam.setRespMsg(ApiRespCodeEnum.SUCCESS.getDesc());
        responseParam.setMchNo(mchNo);
        responseParam.setRandStr(RandomUtil.get32LenStr());
        return responseParam;
    }

    public static ResponseParam unknown(String mchNo){
        ResponseParam responseParam = new ResponseParam();
        responseParam.setRespCode(ApiRespCodeEnum.UNKNOWN.getValue());
        responseParam.setMchNo(mchNo);
        responseParam.setRandStr(RandomUtil.get32LenStr());
        return responseParam;
    }

    public static ResponseParam fail(String mchNo, String respCode, String respMsg){
        if(ApiRespCodeEnum.SUCCESS.getValue().equals(respCode)){
            throw new BizException("错误的响应码：" + respCode);
        }

        ResponseParam responseParam = new ResponseParam();
        responseParam.setRespCode(respCode);
        responseParam.setRespMsg(respMsg);
        responseParam.setMchNo(mchNo);
        responseParam.setRandStr(RandomUtil.get32LenStr());
        return responseParam;
    }

    public void unknownIfEmpty(){
        if(StringUtil.isEmpty(respCode)){
            respCode = ApiRespCodeEnum.UNKNOWN.getValue();
        }
        if(StringUtil.isEmpty(respMsg)){
            respMsg = ApiRespCodeEnum.UNKNOWN.getDesc();
        }
        if(StringUtil.isEmpty(randStr)){
            randStr = RandomUtil.get32LenStr();
        }
    }

    public String toResponseBody(){
        return JsonUtil.toJsonWithNull(this);
    }

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

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
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

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"respCode\":").append(getRespCode()).append(", ")
                .append("\"respMsg\":").append(getRespMsg()).append(", ")
                .append("\"mchNo\":").append(getMchNo()).append(", ")
                .append("\"randStr\":").append(getRandStr()).append(", ")
                .append("\"signType\":").append(getSignType()).append(", ")
                .append("\"secKey\":").append(getSecKey()).append(", ")
                .append("\"data:\"").append(getData().toString())
                .append("}");
        return builder.toString();
    }
}
