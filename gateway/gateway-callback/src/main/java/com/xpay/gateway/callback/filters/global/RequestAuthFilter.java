package com.xpay.gateway.callback.filters.global;

import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.exceptions.GatewayException;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.service.CompanyService;
import com.xpay.gateway.callback.util.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class RequestAuthFilter extends AbstractGlobalFilter {
    private CompanyHelper companyHelper;
    public RequestAuthFilter(CompanyHelper companyHelper){
        this.companyHelper = companyHelper;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_AUTH_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam param = getRequestParam(exchange);

        CompanyEnum company = param.getCompany();
        String signature = param.getSignature();
        boolean isVerifyPass;
        //2.执行验签
        CompanyService companyService = companyHelper.getCompanyService(company);
        isVerifyPass = companyService.isSignaturePass(signature, param.getSignBody());

        //3.验签不通过则直接抛异常返回
        if(!isVerifyPass){
            throw GatewayException.fail(company, param.getPath(), RespCodeEnum.SIGN_FAIL.getValue(), "验签失败");
        }

        //4.验签通过则继续往下走
        TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
        return chain.filter(exchange);
    }
}
