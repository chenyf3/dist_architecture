package com.xpay.sdk.api.entity;

import com.xpay.sdk.api.utils.JsonUtil;

import java.util.Map;
import java.util.TreeMap;

/**
 * 商户请求的VO
 */
public class Request {
    private String method;
    private String version;
    private Object data;
    private String randStr;
    private String signType;
    private String mchNo;
    private String secKey;
    private String timestamp;//时间戳，精确倒毫秒

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> toSortMap(){
        Map<String, String> map = new TreeMap<>();//按字典序排序(升序)
        map.put("method", method);
        map.put("version", version);
        map.put("randStr", randStr);
        map.put("signType", signType);
        map.put("mchNo", mchNo);
        map.put("timestamp", timestamp);
        map.put("data", data instanceof String ? (String) data : JsonUtil.toJson(data));
        if(secKey != null){
            map.put("secKey", secKey);
        }
        return map;
    }

    public void joinSecKey(String key, String iv){
        this.secKey = key + ":" + iv;
    }
}
