package com.xpay.common.api.constants;

/**
 * 公共常量类
 * @author: chenyf
 * @Date: 2018-12-15
 */
public class HttpHeaderKey {
    /**
     * 商户的请求IP
     */
    public static final String REQUEST_ORI_IP_KEY = "REQUEST_ORI_IP";
    /**
     * 商户编号
     */
    public static final String REQUEST_MCH_NO_KEY = "REQUEST_MCH_NO";
    /**
     * 签名类型
     */
    public static final String REQUEST_SIGN_TYPE_KEY = "REQUEST_SIGN_TYPE";
    /**
     * 后端服务告知网关是否把后端服务响应体直接响应给用户而不做任何处理
     */
    public static final String RESP_ORIGINAL_BODY_KEY = "RESP_ORIGINAL_BODY";
    /**
     * 存放签名串的http头名称
     */
    public static final String SIGNATURE_HEADER = "signature";
}
