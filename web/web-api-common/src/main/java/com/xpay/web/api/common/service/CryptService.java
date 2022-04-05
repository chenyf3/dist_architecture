package com.xpay.web.api.common.service;

public interface CryptService {

    public String encryptForWeb(String content, String pubKey);

    public String decryptForWeb(String content, String priKey);

    public String encryptSha1(String content);
}
