package com.xpay.gateway.api.exceptions;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.params.APIParam;
import com.xpay.common.api.utils.SentinelBlockUtil;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.params.ResponseParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.config.conts.ReqCacheKey;
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

    @Autowired
    RequestHelper requestHelper;

    public GatewayExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                   ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
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
        String signature = (String) respAttribute.get(HttpHeaderKey.SIGNATURE_HEADER);
        int httpStatus = getHttpStatus(respAttribute);

        return ServerResponse.status(HttpStatus.valueOf(httpStatus))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(HttpHeaderKey.SIGNATURE_HEADER, signature)
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
        RequestParam requestParam = getRequestParam(request);
        Map<String, Object> respInfoMap = new HashMap<>();
        respInfoMap.put(HTTP_STATUS_KEY, HttpStatus.BAD_REQUEST.value());
        respInfoMap.put(RESPONSE_BODY_KEY, null);
        respInfoMap.put(HttpHeaderKey.SIGNATURE_HEADER, "");

        try {
            //2.参数准备，当异常发生在读请求体的body之前时(如写错Path)，RequestParam是null值，此时无法生成签名串
            String version = requestParam != null ? requestParam.getVersion() : "";
            String mchNo = requestParam != null ? requestParam.getMchNo() : "";
            String signType = requestParam != null ? requestParam.getSignType() : "";
            Throwable ex = super.getError(request);

            //3.为响应体生成签名串
            ResponseParam responseParam = this.buildResponseParam(mchNo, signType, ex);
            byte[] respBodyByte = responseParam.toResponseBody().getBytes(StandardCharsets.UTF_8);
            String signature = requestHelper.genSignature(respBodyByte, mchNo, new APIParam(signType, version));

            //4.设置 http状态码、签名串、源响应体 等信息返回
            respInfoMap.replace(HTTP_STATUS_KEY, selectHttpStatus(ex).value());
            respInfoMap.replace(RESPONSE_BODY_KEY, respBodyByte);
            respInfoMap.replace(HttpHeaderKey.SIGNATURE_HEADER, signature);
        } catch(Throwable e) {
            logger.error("网关全局异常处理器：生成响应数据时出现异常 path = {} RequestParam = {} ",
                    request.exchange().getRequest().getURI().getPath(), requestParam.toString(), e);
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
        if(errorAttributes.containsKey(HTTP_STATUS_KEY) && errorAttributes.get(HTTP_STATUS_KEY) != null){
            httpStatus = HttpStatus.valueOf((int) errorAttributes.get(HTTP_STATUS_KEY));
        }else{
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus.value();
    }

    private void logError(ServerRequest request, HttpStatus httpStatus) {
        Throwable ex = super.getError(request);
        RequestParam requestParam = getRequestParam(request);
        if(requestParam != null){
            requestParam.setSignBody(null);//不需要在日志中打印此数据
        }

        if(ex instanceof GatewayException){
            logger.error("网关处理过程中出现业务判断异常 HttpStatus = {} Exception = {} RequestParam = {}",  httpStatus.value(), ((GatewayException) ex).toMsg(), requestParam.toString());
        }else if(SentinelBlockUtil.isBlockException(ex)){
            logger.error("网关处理过程中发生流控 HttpStatus={} Exception={} RequestParam={}",  httpStatus.value(), SentinelBlockUtil.getBlockMsg(ex), requestParam.toString());
        }else{
            logger.error("网关处理过程中出现未预期异常 HttpStatus = {} RequestParam = {} ", httpStatus.value(), requestParam.toString(), ex);
        }
    }

    private HttpStatus selectHttpStatus(Throwable ex){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof GatewayException) {
            GatewayException e = (GatewayException) ex;
            if(GatewayErrorCode.PARAM_CHECK_ERROR == e.getErrCode()){
                httpStatus = HttpStatus.BAD_REQUEST;
            }else if(GatewayErrorCode.SIGN_VALID_ERROR == e.getErrCode()){
                httpStatus = HttpStatus.FORBIDDEN;
            }else if(GatewayErrorCode.IP_VALID_ERROR == e.getErrCode()){
                httpStatus = HttpStatus.FORBIDDEN;
            }else if(GatewayErrorCode.RATE_LIMIT_ERROR == e.getErrCode()){
                httpStatus = HttpStatus.TOO_MANY_REQUESTS;
            }else if(GatewayErrorCode.IP_BLACK_LIST == e.getErrCode()){
                httpStatus = HttpStatus.FORBIDDEN;
            }else if(GatewayErrorCode.SERVICE_FALLBACK == e.getErrCode()){
                httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
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

    private ResponseParam buildResponseParam(String mchNo, String signType, Throwable ex){
        ResponseParam response;

        if (ex instanceof GatewayException) {
            GatewayException e = (GatewayException) ex;
            response = new ResponseParam();
            response.setRespCode(e.getRespCode());
            response.setRespMsg(e.getRespMsg());
        } else if(ex instanceof TimeoutException) {
            response = ResponseParam.unknown(mchNo);
            response.setRespMsg("Time Out");
        } else if(ex instanceof NotFoundException) { //后端服务无法从注册中心被发现时
            response = new ResponseParam();
            response.setRespCode(ApiRespCodeEnum.PARAM_FAIL.getValue());
            response.setRespMsg("Service Not Found");
        } else if (ex instanceof ResponseStatusException) { //访问没有配置的route path时
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            response = new ResponseParam();
            response.setRespCode(ApiRespCodeEnum.PARAM_FAIL.getValue());
            response.setRespMsg(responseStatusException.getMessage());
        } else if(ex instanceof DecodingException && ex.getCause() != null && ex.getCause() instanceof DataBufferLimitException) {
            response = new ResponseParam();
            response.setRespCode(ApiRespCodeEnum.PARAM_FAIL.getValue());
            response.setRespMsg("请求体大小超限！");
        } else if(SentinelBlockUtil.isBlockException(ex)) {
            response = ResponseParam.unknown(mchNo);
            response.setRespMsg(SentinelBlockUtil.getBlockTypeMsg(ex));
        } else {
            response = ResponseParam.unknown(mchNo);
            response.setRespMsg(response.getRespMsg() == null ? "Internal Server Error" : response.getRespMsg() + ", Internal Server Error");
        }
        response.setMchNo(mchNo);
        response.setSignType(signType);
        response.unknownIfEmpty();
        return response;
    }

    private RequestParam getRequestParam(ServerRequest request){
        Object obj = request.attributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
        if(obj == null){
            return null;
        }else if(obj instanceof RequestParam){
            ((RequestParam) obj).setData(null);//把data置为null，避免日志打印时出现一堆内容
            return (RequestParam) obj;
        }else{
            return null;
        }
    }
}
