package com.xpay.gateway.callback.filters.global;

import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.callback.conts.ReqCacheKey;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.util.IPUtil;
import com.xpay.gateway.callback.util.RequestUtil;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * @description 全局过滤器抽象类，负责处理子类全局过滤器的一些公共逻辑
 * @author chenyf
 * @date 2019-02-23
 */
public abstract class AbstractGlobalFilter implements GlobalFilter, Ordered {
    public final static String PATH_SEPARATOR = "/";

    protected final void cacheRequestParam(ServerWebExchange exchange, RequestParam requestParam){
        RequestUtil.cacheRequestParam(exchange, requestParam);
    }

    protected final RequestParam getRequestParam(ServerWebExchange exchange) {
        return RequestUtil.getRequestParam(exchange);
    }

    protected final String getFirstPath(String path){
        if(StringUtil.isEmpty(path) || PATH_SEPARATOR.equals(path)){
            return PATH_SEPARATOR;
        }

        String firstPath = "";
        String[] pathArr = path.split(PATH_SEPARATOR);
        if(pathArr.length == 1) {
            firstPath = pathArr[0];
        }else if(pathArr.length >= 2){
            firstPath = pathArr[1];
        }
        return PATH_SEPARATOR + firstPath;
    }

    protected final String excludeFirstPath(String path){
        String firstPath = getFirstPath(path);
        int startIndex = firstPath.length();
        String subPath = path.substring(startIndex);
        if(! subPath.startsWith(PATH_SEPARATOR)){
            subPath = PATH_SEPARATOR + subPath;
        }
        return subPath;
    }

    /**
     * 获取客户端的来源IP，在生产环境下，为避免伪造来源IP，需要和服务端的代理配置使用，详情查看：{@link IPUtil#getXRealIpAddress(ServerHttpRequest)}
     * @param exchange
     * @return
     */
    protected final String getRequestRealIp(ServerWebExchange exchange) {
        Object obj = exchange.getAttributes().get(ReqCacheKey.GATEWAY_REQUEST_REAL_IP);
        if (obj != null) {
            return (String) obj;
        }
        String ipAddr = IPUtil.getXRealIpAddress(exchange.getRequest());
        exchange.getAttributes().put(ReqCacheKey.GATEWAY_REQUEST_REAL_IP, ipAddr);
        return ipAddr;
    }
}
