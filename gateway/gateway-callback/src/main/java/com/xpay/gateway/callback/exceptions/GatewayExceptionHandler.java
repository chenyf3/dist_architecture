package com.xpay.gateway.callback.exceptions;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.JsonUtil;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.util.RequestUtil;
import com.xpay.gateway.callback.util.SentinelBlockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @description 全局异常处理器
 * @author chenyf
 */
public class GatewayExceptionHandler extends DefaultErrorWebExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final String HTTP_STATUS_KEY = "httpStatus";
    protected static final String RESPONSE_BODY_KEY = "respBody";
    private CompanyHelper companyHelper;

    public GatewayExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                   ErrorProperties errorProperties, ApplicationContext applicationContext,
                                   CompanyHelper companyHelper) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
        this.companyHelper = companyHelper;
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     * @param errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 渲染响应体
     * @param request
     * @return
     */
    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        boolean includeStackTrace = isIncludeStackTrace(request, MediaType.ALL);
        Map<String, Object> respAttribute = getErrorAttributes(request,
                (includeStackTrace) ? ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE) : ErrorAttributeOptions.defaults());

        byte[] respBodyByte = respAttribute.get(RESPONSE_BODY_KEY) == null ? new byte[]{} : (byte[])respAttribute.get(RESPONSE_BODY_KEY);
        int httpStatus = getHttpStatus(respAttribute);

        return ServerResponse.status(HttpStatus.valueOf(httpStatus))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(respBodyByte))
                .doOnNext(resp -> logError(request, HttpStatus.valueOf(httpStatus)));
    }

    /**
     * 构建响应数据，包括：http状态码、响应体、签名串 等等
     * 此处是gateway处理过程中本身的异常，后端服务的异常不会进入到这里
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        //1.初始化响应数据
        RequestParam requestParam = RequestUtil.getRequestParam(request.exchange());
        Map<String, Object> respInfoMap = new HashMap<>();
        respInfoMap.put(HTTP_STATUS_KEY, HttpStatus.BAD_REQUEST.value());
        respInfoMap.put(RESPONSE_BODY_KEY, new byte[0]);

        try {
            CompanyEnum company = requestParam != null ? requestParam.getCompany() : null;
            //2.参数准备，当异常发生在读请求体的body之前时(如写错Path)，RequestParam是null值，此时无法生成签名串
            Throwable ex = super.getError(request);

            //3.为响应体生成签名串
            Map<String, Object> response = this.buildResponse(company, ex);
            byte[] respBodyByte = JsonUtil.toJson(response).getBytes(StandardCharsets.UTF_8);

            //4.设置 http状态码、签名串、源响应体 等信息返回
            respInfoMap.replace(HTTP_STATUS_KEY, selectHttpStatus(ex).value());
            respInfoMap.replace(RESPONSE_BODY_KEY, respBodyByte);
        } catch(Throwable e) {
            logger.error("网关全局异常处理器：生成响应数据时出现异常 path = {} RequestParam = {} ",
                    request.exchange().getRequest().getURI().getPath(), requestParam==null?null:requestParam.toString(), e);
        }
        return respInfoMap;
    }

    /**
     * 根据code获取对应的HttpStatus
     * @param errorAttributes
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        HttpStatus httpStatus;
        if(errorAttributes.get(HTTP_STATUS_KEY) != null){
            httpStatus = HttpStatus.valueOf((int) errorAttributes.get(HTTP_STATUS_KEY));
        }else{
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus.value();
    }

    private void logError(ServerRequest request, HttpStatus httpStatus) {
        Throwable ex = super.getError(request);
        RequestParam param = RequestUtil.getRequestParam(request.exchange());
        String path = param != null ? param.getPath() : request.path();

        if(ex instanceof GatewayException){
            logger.error("网关处理过程中出现业务判断异常 path = {} HttpStatus = {} Exception = {} RequestParam = {}",  path,
                    httpStatus.value(), ((GatewayException) ex).toMsg(), param==null?null:param.toString());
        }else if(SentinelBlockUtil.isBlockException(ex)){
            logger.error("网关处理过程中发生流控 path = {} HttpStatus={} Exception={} RequestParam={}",  path, httpStatus.value(),
                    SentinelBlockUtil.getBlockMsg(ex), param==null?null:param.toString());
        }else{
            logger.error("网关处理过程中出现未预期异常 path = {} HttpStatus = {} RequestParam = {} ", path, httpStatus.value(),
                    param==null?null:param.toString(), ex);
        }
    }

    private HttpStatus selectHttpStatus(Throwable ex){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof GatewayException) {
            GatewayException e = (GatewayException) ex;
            if(RespCodeEnum.PARAM_FAIL.getValue().equals(e.getCode())){
                httpStatus = HttpStatus.BAD_REQUEST;
            }else if(RespCodeEnum.SIGN_FAIL.getValue().equals(e.getCode())){
                httpStatus = HttpStatus.FORBIDDEN;
            }else if(RespCodeEnum.SYS_FORBID.getValue().equals(e.getCode())){
                httpStatus = HttpStatus.TOO_MANY_REQUESTS;
            }else if(RespCodeEnum.FALLBACK.getValue().equals(e.getCode())){
                httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            }else if(RespCodeEnum.BIZ_FAIL.getValue().equals(e.getCode())){
                httpStatus = HttpStatus.BAD_REQUEST;
            }
        } else if(ex instanceof TimeoutException) {
            httpStatus = HttpStatus.REQUEST_TIMEOUT;
        } else if (ex instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (SentinelBlockUtil.isBlockException(ex)) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            httpStatus = responseStatusException.getStatus();
        } else if(ex instanceof DecodingException && ex.getCause() != null && ex.getCause() instanceof DataBufferLimitException) {
            httpStatus = HttpStatus.PAYLOAD_TOO_LARGE;
        }
        return httpStatus;
    }

    private Map<String, Object> buildResponse(CompanyEnum company, Throwable ex){
        String code;
        String msg;
        if (ex instanceof GatewayException) {
            GatewayException e = (GatewayException) ex;
            code = e.getCode();
            msg = e.getMsg();
        } else if(ex instanceof BizException) {
            code = RespCodeEnum.BIZ_FAIL.getValue();
            msg = ((BizException) ex).getMsg();
        } else if(ex instanceof TimeoutException) {
            code = RespCodeEnum.SYS_ERROR.getValue();
            msg = "Time Out";
        } else if(ex instanceof NotFoundException) { //后端服务无法从注册中心被发现时
            code = RespCodeEnum.PATH_ERROR.getValue();
            msg = "Service Not Found";
        } else if (ex instanceof ResponseStatusException) { //访问没有配置的route path时
            code = RespCodeEnum.PATH_ERROR.getValue();
            msg = "NOT FOUND";
        } else if(ex instanceof DecodingException && ex.getCause() != null && ex.getCause() instanceof DataBufferLimitException) {
            code = RespCodeEnum.SYS_FORBID.getValue();
            msg = "请求体大小超限！";
        } else if(SentinelBlockUtil.isBlockException(ex)) {
            code = RespCodeEnum.FALLBACK.getValue();
            msg = SentinelBlockUtil.getBlockTypeMsg(ex);
        } else {
            code = RespCodeEnum.SYS_ERROR.getValue();
            msg = "Internal Server Error";
        }

        return companyHelper.buildResponse(company, code, msg);
    }
}
