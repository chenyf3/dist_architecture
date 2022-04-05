package com.xpay.web.api.common.service.impl;

import com.xpay.common.utils.RSAUtil;
import com.xpay.web.api.common.service.CryptService;
import org.apache.commons.codec.digest.DigestUtils;

public class DefaultCryptService implements CryptService {
    private String appRsaPubKey;
    private String appRsaPriKey;

    public DefaultCryptService(String appRsaPubKey, String appRsaPriKey){
        this.appRsaPubKey = appRsaPubKey;
        this.appRsaPriKey = appRsaPriKey;
    }

    @Override
    public String encryptForWeb(String content, String pubKey){
        return RSAUtil.encryptForWeb(content, pubKey != null ? pubKey : appRsaPubKey);
    }

    @Override
    public String decryptForWeb(String content, String priKey){
        return RSAUtil.decryptForWeb(content, priKey != null ? priKey : appRsaPriKey);
    }

    @Override
    public String encryptSha1(String content){
        return DigestUtils.sha1Hex(content);
    }
}
