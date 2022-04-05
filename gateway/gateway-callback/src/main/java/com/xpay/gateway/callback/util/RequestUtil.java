package com.xpay.gateway.callback.util;

import com.xpay.gateway.callback.conts.ReqCacheKey;
import com.xpay.gateway.callback.params.RequestParam;
import org.springframework.web.server.ServerWebExchange;

public class RequestUtil {

    public static void cacheRequestParam(ServerWebExchange exchange, RequestParam requestParam){
        exchange.getAttributes().put(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY, requestParam);
    }

    public static RequestParam getRequestParam(ServerWebExchange exchange){
        return exchange.getAttribute(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
    }
}
