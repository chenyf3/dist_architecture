package com.xpay.facade.merchant.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MerchantInfoDto implements Serializable {
    private String mchNo;
    private String shortName;
    private String fullName;
    private Integer mchStatus;
    private Map<String, String> ipValidMap;
    List<SecretKey> secretKeys;

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getMchStatus() {
        return mchStatus;
    }

    public void setMchStatus(Integer mchStatus) {
        this.mchStatus = mchStatus;
    }

    public Map<String, String> getIpValidMap() {
        return ipValidMap;
    }

    public void setIpValidMap(Map<String, String> ipValidMap) {
        this.ipValidMap = ipValidMap;
    }

    public List<SecretKey> getSecretKeys() {
        return secretKeys;
    }

    public void setSecretKeys(List<SecretKey> secretKeys) {
        this.secretKeys = secretKeys;
    }
}
