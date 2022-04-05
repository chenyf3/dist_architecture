package com.xpay.common.api.params;

/**
 * API处理过程中的额外参数，主要用作预留，方便日后做一些拓展变动
 */
public class APIParam {
    /**
     * 签名类型
     */
    private String signType;
    /**
     * 接口版本号
     */
    private String version;

    public APIParam(){}

    public APIParam(String signType, String version){
        this.signType = signType;
        this.version = version;
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
}
