package com.xpay.web.api.common.manager;

import java.util.Map;

/**
 * 对Token进行操作的接口
 * @author Derek
 * @date 2018/04/09
 */
public interface TokenManager {
    String CLAIM_KEY_MCH_NO = "mchNo";
    String CLAIM_KEY_USER_ANENT = "userAgent";

    /**
     * 创建一个token关联上指定用户
     * @param loginName 登录名
	 * @param loginIp 登录IP
	 * @param userAgent 浏览器内核
     * @return 生成的token
     */
    String createAndStoreToken(String loginName, String loginIp, String userAgent, String mchNo);

    /**
     * 检查token是否有效
     * @param token
     * @return 是否有效
     */
    Map<String, String> verifyAndRenewToken(String token);

    /**
     * 校验客户端信息是否匹配，如：IP、UserAgent 等
     * @param claims
     * @param clientIp
     * @param userAgent
     * @param args          预留的其他信息
     * @return
     */
    Boolean validateClientInfo(Map<String, String> claims, String clientIp, String userAgent, String... args);

    /**
     * 从解析后的token中获取登录名
     */
    String getLoginName(Map<String, String> claims);

    /**
     * 把token存储到服务端
     * @param key
     * @param expireSec
     */
    boolean storeTokenToServer(String key, String token, int expireSec);

    /**
     * 从服务端获取token
     * @param key
     */
    String getTokenFromServer(String key);

    /**
     * 清除服务端存储的token
     * @param key 登录用户的id
     */
    void deleteTokenFromServer(String key);
}
