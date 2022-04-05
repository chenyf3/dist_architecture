package com.xpay.gateway.api.filters.global;

import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.config.conts.ReqCacheKey;
import com.xpay.gateway.api.utils.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @description 重写请求到后端服务的url路径
 * @author chenyf
 * @date 2019-02-23
 */
public class RequestRewritePathFilter extends AbstractGlobalFilter {
    private final static String METHOD_SEPARATOR = ".";
    private final static String PATH_SEPARATOR = "/";
    /**
     * 设置当前过滤器的执行顺序：一般来说本过滤器会设置在转发到后端服务之前的最后一个动作
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REWRITE_PATH_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = "";
        if(isTextRequestBody(exchange)){
            method = getRequestParam(exchange).getMethod();
        }else if(isFileRequestBody(exchange)){
            method = getFileUploadParam(exchange).getMethod();
        }

        ServerHttpRequest request = exchange.getRequest();
        String originalPath = request.getURI().getPath();
        String newPath = convertMethodToPath(method);
        ServerHttpRequest newRequest = request.mutate()
                .path(newPath)
                .build();

        exchange.getAttributes().put(ReqCacheKey.GATEWAY_ORIGINAL_REQUEST_PATH_ATTR, originalPath);
        exchange.getAttributes().put(ReqCacheKey.GATEWAY_ORIGINAL_REQUEST_FULL_PATH_ATTR, originalPath + newPath);
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());
        TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    private String convertMethodToPath(String method){
        if (StringUtil.isEmpty(method)) {
            return PATH_SEPARATOR;
        }
        String path = method.replace(METHOD_SEPARATOR, PATH_SEPARATOR);
        if(! path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        return subPathEnd(path, PATH_SEPARATOR, 0);
    }
}
