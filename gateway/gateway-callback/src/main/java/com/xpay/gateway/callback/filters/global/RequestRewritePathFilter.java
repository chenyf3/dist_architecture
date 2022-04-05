package com.xpay.gateway.callback.filters.global;

import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.conts.ReqCacheKey;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.util.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @description 重写请求到后端服务的url路径
 * @author chenyf
 */
public class RequestRewritePathFilter extends AbstractGlobalFilter {
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
        RequestParam requestParam = getRequestParam(exchange);
        String originalPath = requestParam.getPath();
        String newPath = excludeFirstPath(originalPath);
        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .path(newPath)
                .build();

        exchange.getAttributes().put(ReqCacheKey.GATEWAY_ORIGINAL_REQUEST_PATH_ATTR, originalPath);
        exchange.getAttributes().put(ReqCacheKey.GATEWAY_ORIGINAL_REQUEST_FULL_PATH_ATTR, originalPath + newPath);
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());

        TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
        return chain.filter(exchange.mutate().request(newRequest).build());
    }
}
