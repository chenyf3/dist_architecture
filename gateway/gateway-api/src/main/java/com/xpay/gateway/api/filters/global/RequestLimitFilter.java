package com.xpay.gateway.api.filters.global;

import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.config.properties.GatewayProperties;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.utils.TraceUtil;
import com.xpay.starter.plugin.plugins.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @description 请求限流过滤器
 * @author chenyf
 * @date 2020-10-10
 */
public class RequestLimitFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RateLimiter rateLimiter;
    private GatewayProperties gatewayProperties;

    public RequestLimitFilter(RateLimiter rateLimiter, GatewayProperties gatewayProperties){
        this.rateLimiter = rateLimiter;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_LIMIT_FILTER;
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam requestParam = getRequestParam(exchange);
        String method = requestParam.getMethod();
        String mchNo = requestParam.getMchNo();
        return commonFilter(exchange, chain, method, mchNo);
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        FileUploadParam uploadParam = getFileUploadParam(exchange);
        String method = uploadParam.getMethod();
        String mchNo = uploadParam.getMchNo();
        return commonFilter(exchange, chain, method, mchNo);
    }

    private Mono<Void> commonFilter(ServerWebExchange exchange, GatewayFilterChain chain, String method, String mchNo){
        String path = exchange.getRequest().getURI().getPath();
        GatewayProperties.Rate rateConfig = getRateConfig(path, method, mchNo);
        if(rateConfig == null || rateConfig.getReplenishRate() == GatewayProperties.NOT_LIMIT_RATE_VALUE){ //没有配置限流或者限流关闭时直接通过
            return chain.filter(exchange);
        }

        String key = resolveKey(path, method, mchNo);
        boolean isAcquired = rateLimiter.tryAcquire(key, rateConfig.getReplenishRate(), rateConfig.getBurstCapacity());
        if(isAcquired){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }

        logger.warn("Path={} method={} mchNo={} ReplenishRate={} BurstCapacity={} 被执行限流", path,
                method, mchNo, rateConfig.getReplenishRate(), rateConfig.getBurstCapacity());
        throw GatewayException.fail(ApiRespCodeEnum.SYS_FORBID.getValue(), "请求过于频繁，请稍后再试！", GatewayErrorCode.RATE_LIMIT_ERROR);
    }

    private String resolveKey(String path, String method, String mchNo) {
        // path + method + mchNo 作为限流的key
        return path + ":" + method + ":" + mchNo;
    }

    private GatewayProperties.Rate getRateConfig(String path, String method, String mchNo){
        Map<String, GatewayProperties.PathConf> pathConf = this.gatewayProperties.getPathConf();
        if(pathConf.get(path) == null){ //当前 Path 没有配置，表示不限流，直接返回null即可
            return null;
        }

        Map<String, GatewayProperties.MethodConf> methodConf = pathConf.get(path).getMethodConf();
        if(methodConf.get(method) == null){ //当前 method 没有配置，返回当前 path 下的默认配置
            return pathConf.get(path).getDefaultRate();
        }

        Map<String, GatewayProperties.Rate> mchRate = methodConf.get(method).getMchRate();
        if(mchRate.get(mchNo) == null){ //当前 mchNo 没有配置，返回当前 method 下的默认配置
            return methodConf.get(method).getDefaultRate();
        }else{
            return mchRate.get(mchNo); //返回当前商户的配置
        }
    }
}
