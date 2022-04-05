package com.xpay.gateway.callback.conts;

/**
 * @description 在ServerRequest中缓存的key
 * @author: chenyf
 * @Date: 2021-10-27
 */
public class ReqCacheKey {
    public static final String CACHE_TRACE_ID_KEY = "cacheRequestTraceId";
    public static final String CACHE_TRACE_TIME_KEY = "cacheRequestTraceTime";
    public static final String GATEWAY_REQUEST_BODY_TYPE = "requestBodyType";
    public static final String GATEWAY_REQUEST_REAL_IP = "requestRealIp";

    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    public static final String GATEWAY_ORIGINAL_REQUEST_PATH_ATTR = "gatewayOriginalRequestPath";
    public static final String GATEWAY_ORIGINAL_REQUEST_FULL_PATH_ATTR = "gatewayOriginalRequestFullPath";
    public static final String ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR = "original_response_content_type";

}
