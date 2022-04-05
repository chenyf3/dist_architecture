package com.xpay.gateway.callback.filters.global;

import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.callback.config.GatewayProperties;
import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.exceptions.GatewayException;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.util.TraceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * @description 黑名单过滤器，可处理IP黑名单、商户黑名单等等
 * @author chenyf
 */
public class BlackListFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private GatewayProperties properties;

    public BlackListFilter(GatewayProperties properties){
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return FilterOrder.IP_BLACKLIST_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        TraceUtil.genTraceId(exchange);

        if(StringUtil.isEmpty(properties.getIpBlackList())){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }

        String ip = getRequestRealIp(exchange);
        if(this.isIPPass(properties.getIpBlackList(), ip)){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }

        String oriPath = exchange.getRequest().getURI().getPath();
        String firstPath = getFirstPath(oriPath);
        CompanyEnum company = CompanyHelper.determineCompany(firstPath);
        logger.warn("ip = {} 被列为黑名单，禁止访问！ pattern = {}", ip, properties.getIpBlackList());
        throw GatewayException.fail(company, oriPath, RespCodeEnum.SYS_FORBID.getValue(), "请求受限");
    }

    private boolean isIPPass(String pattern, String ip){
        if(ip == null || ip.trim().length() == 0){
            return false;
        }
        return !Pattern.matches(pattern, ip);
    }
}
