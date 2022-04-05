package com.xpay.sdk.api.entity;

public class SecretKey {
    /**
     * 商户私钥，作用有二：
     *  1、为请求报文生成签名摘要
     *  2、对响应报文的sec_key进行解密(如果有)
     */
    private String mchPriKey;

    /**
     * 平台公钥，作用有二
     *  1、对响应报文进行验签
     *  2、对请求报文的sec_key进行加密(如果有)
     */
    private String platPubKey;


    public String getMchPriKey() {
        return mchPriKey;
    }

    public void setMchPriKey(String mchPriKey) {
        this.mchPriKey = mchPriKey;
    }

    public String getPlatPubKey() {
        return platPubKey;
    }

    public void setPlatPubKey(String platPubKey) {
        this.platPubKey = platPubKey;
    }
}
