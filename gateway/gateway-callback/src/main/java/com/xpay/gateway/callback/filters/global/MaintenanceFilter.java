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

import java.util.Arrays;

/**
 * @description 业务线维护过滤器，主要用以某些业务线需要内部维护而要求停止对外服务时使用
 * @author chenyf
 * @date 2019-02-23
 */
public class MaintenanceFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(MaintenanceFilter.class);
    private GatewayProperties properties;

    public MaintenanceFilter(GatewayProperties properties){
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return FilterOrder.BIZ_OFF_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String oriPath = exchange.getRequest().getURI().getPath();
        String firstPath = getFirstPath(oriPath);
        CompanyEnum company = CompanyHelper.determineCompany(firstPath);
        String maintenancePath = properties.getMaintenancePath();

        if(this.isReject(firstPath, maintenancePath)){
            logger.warn("oriPath = {} maintenancePath = {} 业务受限，业务配置禁止访问", oriPath, maintenancePath);
            throw GatewayException.fail(company, oriPath, RespCodeEnum.SYS_FORBID.getValue(), "系统维护");
        }else{
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }
    }

    private boolean isReject(String path, String maintenancePath){
        if(StringUtil.isEmpty(maintenancePath)){
            return false;
        }

        return "ALL".equals(maintenancePath)
                || Arrays.asList(maintenancePath.split(",")).contains(path);
    }
}
