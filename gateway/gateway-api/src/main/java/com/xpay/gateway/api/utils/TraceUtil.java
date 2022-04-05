package com.xpay.gateway.api.utils;

import com.xpay.common.utils.RandomUtil;
import com.xpay.gateway.api.config.conts.ReqCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;

public class TraceUtil {
    private static final Logger logger = LoggerFactory.getLogger(TraceUtil.class);

    public static void genTraceId(ServerWebExchange exchange) {
        exchange.getAttributes().put(ReqCacheKey.CACHE_TRACE_ID_KEY, RandomUtil.get32LenStr());
        exchange.getAttributes().put(ReqCacheKey.CACHE_TRACE_TIME_KEY, System.currentTimeMillis());
    }

    public static String getTraceId(ServerWebExchange exchange) {
        Object traceObj = exchange.getAttributes().get(ReqCacheKey.CACHE_TRACE_ID_KEY);
        return traceObj == null ? null : (String)traceObj;
    }

    public static void timeTraceLog(ServerWebExchange exchange, String filterName){
        Object traceObj = exchange.getAttributes().get(ReqCacheKey.CACHE_TRACE_ID_KEY);
        Object timeObj = exchange.getAttributes().get(ReqCacheKey.CACHE_TRACE_TIME_KEY);
        if(traceObj != null && timeObj != null && logger.isDebugEnabled()) {
            logger.info("执行完毕 traceId: {} {}(ms) filter: {}", traceObj, (System.currentTimeMillis()-(long)timeObj), filterName);
        }
        exchange.getAttributes().put(ReqCacheKey.CACHE_TRACE_TIME_KEY, System.currentTimeMillis());
    }

    public static void bodyLog(ServerWebExchange exchange, String bodyLog){
        String traceId = getTraceId(exchange);
        if(traceId != null && logger.isDebugEnabled()) {
            String clientVersion = exchange.getRequest().getHeaders().getFirst("CLIENT-VERSION");
            logger.info("请求体读取完成 traceId: {} client:{} body: {}", traceId, clientVersion, bodyLog);
        }
    }
}
