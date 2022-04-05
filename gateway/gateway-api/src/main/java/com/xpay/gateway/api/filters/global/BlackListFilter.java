package com.xpay.gateway.api.filters.global;

import com.xpay.gateway.api.config.properties.GatewayProperties;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.utils.TraceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * @description 黑名单过滤器，可处理IP黑名单、商户黑名单等等
 * @author chenyf
 * @date 2019-02-23
 */
public class BlackListFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private GatewayProperties gatewayProperties;

    public BlackListFilter(GatewayProperties gatewayProperties){
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public int getOrder() {
        return FilterOrder.IP_BLACKLIST_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        TraceUtil.genTraceId(exchange);

        if(StringUtil.isEmpty(gatewayProperties.getIpBlackList())){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }

        String ip = getRequestRealIp(exchange);
        if(this.isIPPass(gatewayProperties.getIpBlackList(), ip)){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }

        logger.warn("ip = {} 被列为黑名单，禁止访问！ pattern = {}", ip, gatewayProperties.getIpBlackList());
        throw GatewayException.fail(ApiRespCodeEnum.SYS_FORBID.getValue(), "请求受限", GatewayErrorCode.IP_BLACK_LIST);
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    private boolean isIPPass(String pattern, String ip){
        if(ip == null || ip.trim().length() == 0){
            return false;
        }
        return !Pattern.matches(pattern, ip);
    }
}
