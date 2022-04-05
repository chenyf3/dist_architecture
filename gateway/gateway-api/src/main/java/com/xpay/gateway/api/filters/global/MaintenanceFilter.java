package com.xpay.gateway.api.filters.global;

import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.config.properties.GatewayProperties;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.utils.TraceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @description 业务线维护过滤器，主要用以某些业务线需要内部维护而要求停止对外服务时使用
 * @author chenyf
 * @date 2019-02-23
 */
public class MaintenanceFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(MaintenanceFilter.class);
    private GatewayProperties gatewayProperties;

    public MaintenanceFilter(GatewayProperties gatewayProperties){
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public int getOrder() {
        return FilterOrder.BIZ_OFF_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String originalPath = subPathEnd(req.getURI().getPath(), "/", 0);
        String maintenancePath = gatewayProperties.getMaintenancePath();

        if(this.isReject(originalPath, maintenancePath)){
            logger.warn("originalPath = {} maintenancePath = {} 业务受限，业务配置禁止访问", originalPath, maintenancePath);
            throw GatewayException.fail(ApiRespCodeEnum.SYS_FORBID.getValue(), "系统维护", GatewayErrorCode.REQUEST_FORBID);
        }else{
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    private boolean isReject(String path, String maintenancePath){
        if(StringUtil.isEmpty(maintenancePath)){
            return false;
        }

        return "ALL".equals(maintenancePath)
                || Arrays.asList(maintenancePath.split(",")).contains(path);
    }
}
