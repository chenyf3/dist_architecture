package com.xpay.web.api.common.manager;

public interface CodeManager {

    /**
     * 缓存验证码
     * @param codeKey
     * @param code
     * @return
     */
    public boolean cacheCode(String codeKey, String code, int expireSec, String limitKey);

    /**
     * 获取验证码
     * @param codeKey
     * @return
     */
    public String getCode(String codeKey);

    /**
     * 删除验证码
     * @param codeKey
     */
    public void deleteCode(String codeKey, String limitKey);

    /**
     * 获取限流值
     * @param limitKey
     * @return
     */
    public String getLimitVal(String limitKey);
}
