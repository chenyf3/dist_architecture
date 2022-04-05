package com.xpay.common.api.params;

import com.xpay.common.api.constants.IPValidKeyConst;

import java.io.Serializable;
import java.util.Map;

/**
 * 用户信息
 * @author chenyf
 * @date 2018-12-15
 */
public class MchInfo implements Serializable {
    private static final long serialVersionUID = 6654122563255411254L;

    /**
     * 商户编号
     */
    private String mchNo;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 商户状态
     */
    private Integer mchStatus;

    /**
     * 签名类型
     */
    private String signType;

    /**
     * 验签的密钥，当signType为MD5时，此属性的值为平台和用户共享的密钥，当signType为RSA时，此属性的值为用户放在平台用以验签的RSA公钥
     */
    private String signValidKey;

    /**
     * 生成签名的密钥，当signType为MD5时，此属性的值为平台和用户共享的密钥，当signType为RSA时，此属性的值为平台的私钥
     */
    private String signGenKey;

    /**
     * 平台接收请求时，对secKey进行解密的私钥(平台的私钥)
     */
    private String secKeyDecryptKey;

    /**
     * 汇聚响应时，对secKey进行加密的公钥(商户的公钥)
     */
    private String secKeyEncryptKey;

    /**
     * 如果需要进行IP校验，则需要设置值，此Map的key参照 {@link IPValidKeyConst}
     */
    private Map<String, String> ipValidMap;

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getMchName() {
        return mchName;
    }

    public void setMchName(String mchName) {
        this.mchName = mchName;
    }

    public Integer getMchStatus() {
        return mchStatus;
    }

    public void setMchStatus(Integer mchStatus) {
        this.mchStatus = mchStatus;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSignValidKey() {
        return signValidKey;
    }

    public void setSignValidKey(String signValidKey) {
        this.signValidKey = signValidKey;
    }

    public String getSignGenKey() {
        return signGenKey;
    }

    public void setSignGenKey(String signGenKey) {
        this.signGenKey = signGenKey;
    }

    public String getSecKeyDecryptKey() {
        return secKeyDecryptKey;
    }

    public void setSecKeyDecryptKey(String secKeyDecryptKey) {
        this.secKeyDecryptKey = secKeyDecryptKey;
    }

    public String getSecKeyEncryptKey() {
        return secKeyEncryptKey;
    }

    public void setSecKeyEncryptKey(String secKeyEncryptKey) {
        this.secKeyEncryptKey = secKeyEncryptKey;
    }

    public Map<String, String> getIpValidMap() {
        return ipValidMap;
    }

    public void setIpValidMap(Map<String, String> ipValidMap) {
        this.ipValidMap = ipValidMap;
    }
}
