package com.xpay.gateway.api.filters.global;

import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.gateway.api.utils.IPUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import com.xpay.gateway.api.config.conts.ReqCacheKey;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @description 全局过滤器抽象类，负责处理子类全局过滤器的一些公共逻辑
 * @author chenyf
 * @date 2019-02-23
 */
public abstract class AbstractGlobalFilter implements GlobalFilter, Ordered {
    protected static final Integer TEXT_BODY_TYPE = 1;
    protected static final Integer FILE_FORM_BODY_TYPE = 2;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if(isTextRequestBody(exchange)){
            return textBodyFilter(exchange, chain);
        }else if(isFileRequestBody(exchange)){
            return fileBodyFilter(exchange, chain);
        }else{
            return chain.filter(exchange);
        }
    }

    protected final boolean isTextRequestBody(ServerWebExchange exchange){
        Integer bodyType = (Integer) exchange.getAttributes().get(ReqCacheKey.GATEWAY_REQUEST_BODY_TYPE);
        return TEXT_BODY_TYPE.equals(bodyType);
    }

    protected final boolean isFileRequestBody(ServerWebExchange exchange){
        Integer bodyType = (Integer) exchange.getAttributes().get(ReqCacheKey.GATEWAY_REQUEST_BODY_TYPE);
        return FILE_FORM_BODY_TYPE.equals(bodyType);
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

    protected final void cacheRequestParam(ServerWebExchange exchange, RequestParam requestParam){
        exchange.getAttributes().put(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY, requestParam);
        exchange.getAttributes().put(ReqCacheKey.GATEWAY_REQUEST_BODY_TYPE, TEXT_BODY_TYPE);
    }

    protected final void cacheFileUploadParam(ServerWebExchange exchange, FileUploadParam fileUploadParam){
        exchange.getAttributes().put(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY, fileUploadParam);
        exchange.getAttributes().put(ReqCacheKey.GATEWAY_REQUEST_BODY_TYPE, FILE_FORM_BODY_TYPE);
    }

    protected final RequestParam getRequestParam(ServerWebExchange exchange){
        return (RequestParam) exchange.getAttributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
    }

    protected final FileUploadParam getFileUploadParam(ServerWebExchange exchange){
        return (FileUploadParam) exchange.getAttributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
    }

    protected final String subPathEnd(String path, String pattern, int count){
        if(count > 3){ //避免商户不规范传入url时进入死循环
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), "请求路径不正确", GatewayErrorCode.PARAM_CHECK_ERROR);
        }else if(path.endsWith(pattern)){
            path = path.substring(0, path.length()-1);
            return subPathEnd(path, pattern, count+1);
        }else{
            return path;
        }
    }

    protected abstract Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain);

    protected abstract Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain);
}
