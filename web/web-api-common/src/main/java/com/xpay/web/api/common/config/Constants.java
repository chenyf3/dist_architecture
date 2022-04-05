package com.xpay.web.api.common.config;

/**
 * 常量
 * @author Derek
 * @date 2018/04/09
 */
public class Constants {
    /**--------------------------- HTTP相关 START ---------------------------*/
    /**
     * Http头中存放Authorization的字段
     */
    public static final String HTTP_TOKEN_HEADER = "X-TOKEN";
    /**--------------------------- HTTP相关 END ---------------------------*/


    /**--------------------------- HttpServletRequest相关 START ---------------------------*/
    /**
     * HttpServletRequest中存储当前用户登录名的key
     */
    public static final String REQUEST_USER_LOGIN_NAME = "USER_LOGIN_NAME";
    /**
     * HttpServletRequest中存储当前用户商户编号的key
     */
    public static final String REQUEST_USER_MCH_NO = "USER_MCH_NO";
    /**
     * HttpServletRequest中存储当前用户IP的key
     */
    public static final String REQUEST_USER_IP = "USER_IP";
    /**
     * HttpServletRequest中存储异常码的key
     */
    public static final String REQUEST_EXCEPTION_CODE = "EXCEPTION_CODE";
    /**
     * HttpServletRequest中存储异常描述的key
     */
    public static final String REQUEST_EXCEPTION_MSG = "EXCEPTION_MSG";
    /**--------------------------- HttpServletRequest相关 END ---------------------------*/


    /**--------------------------- 缓存相关 START ---------------------------*/
    /**
     * 缓存当前用户权限的key
     */
    public static final String CACHE_LOGIN_TOKEN_FLAG = "LOGIN_TOKEN_FLAG_";
    /**
     * 缓存当前用户权限的key
     */
    public static final String CACHE_PERMISSION_FLAG = "PERMISSION_FLAG_";

    /**
     * 是否已经通过密钥修改手机验证
     */
    public static final String IS_CHECK_PHONE_CODE_OF_MERCHANT_KEY = "IS_CHECK_PHONE_CODE_OF_MERCHANT_KEY";

    /**
     * Redis中用户基本参数的KEY值
     */
    public static final String USER_BASE_PARAM_KEY = "USER_BASE_PARAM_KEY_NEW";

    /**
     * 当前用户的手机验证码
     */
    public static final String CURRENT_USER_PHONE_CODE = "CURRENT_USER_PHONE_CODE";

    /**
     * 交易密码重置时的缓存key
     */
    public static final String TRADE_PWD_RESET_KEY = "TRADE_PWD_RESET_";

    /**--------------------------- 缓存相关 END ---------------------------*/
}
