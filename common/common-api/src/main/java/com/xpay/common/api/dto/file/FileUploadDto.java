package com.xpay.common.api.dto.file;

public class FileUploadDto {
    private String method;
    private String randStr;
    private String version;
    private String signType;
    private String mchNo;
    private String secKey;
    private String timestamp; //时间戳，精确倒毫秒
    private String hash; //文件的hash值，如：md5值，上传多个文件时用英文的逗号分割
    private String extras; //额外的参数(如有需要，可以使用)

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
}
