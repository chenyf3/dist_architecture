package com.xpay.gateway.callback.filters.global;

import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.service.CompanyService;
import com.xpay.gateway.callback.util.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @description 修改请求体，包括：secKey的解密 等
 * @author chenyf
 */
public class RequestModifyFilter extends AbstractGlobalFilter {
    private CompanyHelper companyHelper;

    public RequestModifyFilter(CompanyHelper companyHelper){
        this.companyHelper = companyHelper;
    }

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序建议为第4个
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_MODIFY_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam requestParam = getRequestParam(exchange);
        //1.重新封装请求体、计算请求体的字节大小
        byte[] bodyByte = modifyBody(exchange, requestParam);
        String contentType = MediaType.APPLICATION_JSON_VALUE;
        long contentLength = bodyByte.length;

        //2.设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(contentType));
        headers.setContentLength(contentLength);

        //3.更新转发到后端服务的body
        Mono modifiedBody = Mono.just(bodyByte);
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, byte[].class);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    ServerHttpRequestDecorator decorator = decorateServer(exchange, headers, outputMessage);
                    TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
                    return chain.filter(exchange.mutate().request(decorator).build());
                }));
    }

    private byte[] modifyBody(ServerWebExchange exchange, RequestParam requestParam) {
        //1.修改请求体(如解密字段
        CompanyService companyService = companyHelper.getCompanyService(requestParam.getCompany());
        companyService.modifyRequestParam(requestParam);

        //2.更新缓存中的body
        cacheRequestParam(exchange, requestParam);

        //3.把请求体转换成字节数组
        return requestParam.getBody();
    }

    private ServerHttpRequestDecorator decorateServer(ServerWebExchange exchange, HttpHeaders headers,
                                        CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");//标识请求体大小不确定，分块传输，接收端将以最后一个大小为0的块为结束
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }
}
