package com.xpay.sdk.api.entity;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileRequest {
    private String method;
    private String version;
    private String randStr;
    private String signType;
    private String mchNo;
    private String secKey;
    private String timestamp; //时间戳，精确倒毫秒
    private String extras; //额外的参数(如有需要，可以使用)
    private String hash; //文件的hash值，如：md5值
    private List<FileInfo> files;

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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    public Map<String, String> getTextBodySort(){
        Map<String, String> textParam = new TreeMap<>();
        textParam.put("method", getMethod());
        textParam.put("version", getVersion());
        textParam.put("randStr", getRandStr());
        textParam.put("signType", getSignType());
        textParam.put("mchNo", getMchNo());
        textParam.put("secKey", getSecKey());
        textParam.put("timestamp", getTimestamp());
        textParam.put("extras", getExtras());
        textParam.put("hash", getHash());
        return textParam;
    }
}
