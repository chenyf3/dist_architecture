package com.xpay.facade.merchant.dto;

import java.io.Serializable;

public class SecretKey implements Serializable {
    private Integer signType;//签名类型
    private String mchPublicKey;//商户公钥
    private String platPublicKey;//平台公钥
    private String platPrivateKey;//平台私钥

    public Integer getSignType() {
        return signType;
    }

    public void setSignType(Integer signType) {
        this.signType = signType;
    }

    public String getMchPublicKey() {
        return mchPublicKey;
    }

    public void setMchPublicKey(String mchPublicKey) {
        this.mchPublicKey = mchPublicKey;
    }

    public String getPlatPublicKey() {
        return platPublicKey;
    }

    public void setPlatPublicKey(String platPublicKey) {
        this.platPublicKey = platPublicKey;
    }

    public String getPlatPrivateKey() {
        return platPrivateKey;
    }

    public void setPlatPrivateKey(String platPrivateKey) {
        this.platPrivateKey = platPrivateKey;
    }
}
