package com.xpay.common.api.webflux;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 使用webflux时的全局异常处理器，负责决定http状态码等非响应体的工作
 */
public class GlobalExceptionHandler extends DefaultErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                  ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {
        final Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        return ServerResponse.status(HttpStatus.OK)//统一返回200
                .contentType(MediaType.APPLICATION_JSON_UTF8)//统一返回utf8编码的json
                .body(BodyInserters.fromObject(errorPropertiesMap));
    }
}