package com.xpay.gateway.api.config.conts;

/**
 * @description 内部错误码，供网关内部处理使用
 * @author: chenyf
 * @Date: 2019-02-27
 */
public class GatewayErrorCode {
    public final static int PARAM_CHECK_ERROR = 1;//验参不通过
    public final static int SIGN_VALID_ERROR = 2;//验签失败
    public final static int IP_VALID_ERROR = 3;//IP校验失败
    public final static int RATE_LIMIT_ERROR = 4;//限流
    public final static int IP_BLACK_LIST = 5;//被列入IP黑名单
    public final static int REQUEST_FORBID = 6;//请求受限
    public final static int SERVICE_FALLBACK = 7;//服务降级


}
