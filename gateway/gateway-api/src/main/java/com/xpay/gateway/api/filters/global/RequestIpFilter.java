package com.xpay.gateway.api.filters.global;

import com.xpay.common.api.params.APIParam;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.service.ValidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 来源IP校验过滤器
 */
public class RequestIpFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RequestHelper requestHelper;
    private ValidService validService;

    public RequestIpFilter(RequestHelper requestHelper, ValidService validService){
        this.requestHelper = requestHelper;
        this.validService = validService;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_IP_VALID_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = getRequestRealIp(exchange);
        RequestParam requestParam = getRequestParam(exchange);

        boolean isVerifyOk = false;
        String mchNo = requestParam.getMchNo();
        String msg = "";
        String expectIp = "";
        try {
            isVerifyOk = requestHelper.ipVerify(mchNo, ip, null, new APIParam(requestParam.getSignType(), requestParam.getVersion()));
            if(! isVerifyOk){
                msg = "非法来源";
            }
        } catch (Throwable e) {
            msg = "请求来源校验异常";
            isVerifyOk = false;
            logger.error("IP校验失败异常 RequestParam = {}", requestParam.toString(), e);
        }

        if (! isVerifyOk) {
            try{
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                validService.afterIpValidFail(route.getId(), ip, expectIp, requestParam);
            }catch(Throwable e){
                logger.error("IP校验失败，IP校验失败处理器有异常 RequestParam = {}", requestParam.toString(), e);
            }
        }

        if (isVerifyOk) {
            return chain.filter(exchange);
        } else {
            throw GatewayException.fail(ApiRespCodeEnum.SYS_FORBID.getValue(), msg, GatewayErrorCode.IP_VALID_ERROR);
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
}
