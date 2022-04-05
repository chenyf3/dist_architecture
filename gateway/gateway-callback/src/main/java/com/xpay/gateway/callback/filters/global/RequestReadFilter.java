package com.xpay.gateway.callback.filters.global;

import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.callback.config.GatewayProperties;
import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.exceptions.GatewayException;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.service.CompanyService;
import com.xpay.gateway.callback.util.IPUtil;
import com.xpay.gateway.callback.util.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestReadFilter extends AbstractGlobalFilter {
    private CompanyHelper companyHelper;
    private GatewayProperties properties;

    public RequestReadFilter(CompanyHelper companyHelper, GatewayProperties properties){
        this.companyHelper = companyHelper;
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_READ_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String ip = IPUtil.getIpAddress(request);
        String path = request.getURI().getPath();
        String method = request.getMethodValue();
        String firstPath = getFirstPath(path);
        CompanyEnum company = CompanyHelper.determineCompany(firstPath);
        Map<String, String> headerMap = fetchHeaders(request);

        RequestParam requestParam = new RequestParam();
        requestParam.setIp(ip);
        requestParam.setPath(path);
        requestParam.setMethod(method);
        requestParam.setHeaders(headerMap);
        requestParam.setCompany(company);

        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        if(HttpMethod.GET.equals(exchange.getRequest().getMethod())){
            return readQueryParam(exchange, chain, requestParam);
        }else if(MediaType.APPLICATION_JSON.includes(mediaType)){
            return readJsonBody(exchange, chain, requestParam);
        }else if(MediaType.APPLICATION_FORM_URLENCODED.equals(mediaType)){
            return readUrlEncodeForm(exchange, chain, requestParam);
        }else{
            throw GatewayException.fail(company, path, RespCodeEnum.PARAM_FAIL.getValue(), "ContentType : " + mediaType + " Are Not Support!");
        }
    }

    private Mono<Void> readQueryParam(ServerWebExchange exchange, GatewayFilterChain chain, RequestParam requestParam) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();

        Map<String, String> paramMap = new TreeMap<>(); //按字典序排序(升序)
        for(Map.Entry<String, String> entry : queryParams.toSingleValueMap().entrySet()){ //此处直接规定了一个key只能有一个值
            paramMap.put(entry.getKey(), entry.getValue());
        }

        String bodyStr = JsonUtil.toJson(paramMap);
        byte[] bodyBytes = bodyStr.getBytes(StandardCharsets.UTF_8);

        requestParam.setBody(bodyBytes);
        fillRequestParam(requestParam);

        super.cacheRequestParam(exchange, requestParam);

        TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
        return chain.filter(exchange);
    }

    private Mono<Void> readJsonBody(ServerWebExchange exchange, GatewayFilterChain chain, RequestParam requestParam){
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap((DataBuffer dataBuffer) -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);//释放资源

                    requestParam.setBody(bodyBytes);
                    fillRequestParam(requestParam);

                    super.cacheRequestParam(exchange, requestParam);

                    TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> readUrlEncodeForm(ServerWebExchange exchange, GatewayFilterChain chain, RequestParam requestParam) {
        return exchange.getFormData().flatMap(multiFormMap-> {
            Map<String, String> paramMap = new TreeMap<>(); //按字典序排序(升序)
            for(Map.Entry<String, String> entry : multiFormMap.toSingleValueMap().entrySet()){ //此处直接规定了一个key只能有一个值
                paramMap.put(entry.getKey(), entry.getValue());
            }

            String bodyStr = JsonUtil.toJson(paramMap);
            byte[] bodyBytes = bodyStr.getBytes(StandardCharsets.UTF_8);

            requestParam.setBody(bodyBytes);
            fillRequestParam(requestParam);

            super.cacheRequestParam(exchange, requestParam);

            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        });
    }

    private Map<String, String> fetchHeaders(ServerHttpRequest request){
        if (StringUtil.isEmpty(properties.getFetchHeaders())) {
            return Collections.emptyMap();
        }

        Map<String, String> headerMap = new HashMap<>();
        HttpHeaders headers = request.getHeaders();
        String[] headerArr = properties.getFetchHeaders().split(",");
        for (String header : headerArr) {
            String headerVal = headers.getFirst(header);
            if(headerVal != null) {
                headerMap.put(header, headerVal);
            }
        }
        return headerMap;
    }

    private void fillRequestParam(RequestParam requestParam){
        CompanyService companyService = companyHelper.getCompanyService(requestParam.getCompany());
        companyService.fillSignInfo(requestParam);
    }
}
