package com.xpay.gateway.api.filters.global;

import com.xpay.gateway.api.config.properties.GatewayProperties;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.statics.enums.common.SignTypeEnum;
import com.xpay.common.utils.StringUtil;
import com.xpay.common.utils.ValidateUtil;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.utils.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @description 请求参数校验，这个过滤器必须是在第2个，不然，后续的过滤器可能会会获取到错误的参数，或者因为某个参数为null而报空指针
 * @author chenyf
 * @date 2019-02-23
 */
public class RequestParamCheckFilter extends AbstractGlobalFilter {
    private GatewayProperties gatewayProperties;

    public RequestParamCheckFilter(GatewayProperties gatewayProperties){
        this.gatewayProperties = gatewayProperties;
    }

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序必须为第2个，不然，后续的过滤器拿取参数时可能会出现空指针异常
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_PARAM_CHECK_FILTER;
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam requestParam = getRequestParam(exchange);
        String path = exchange.getRequest().getURI().getPath();
        String msg = requestPathValid(path);
        if(StringUtil.isEmpty(msg)){
            msg = textParamValid(requestParam, path);
        }

        if(StringUtil.isEmpty(msg)){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }else{
            //抛出异常，由全局异常处理器来处理响应信息
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), msg, GatewayErrorCode.PARAM_CHECK_ERROR);
        }
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String msg = requestPathValid(path);
        if(StringUtil.isEmpty(msg)){
            msg = fileParamValid(getFileUploadParam(exchange), path);
        }
        if(StringUtil.isEmpty(msg)){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }else{
            //抛出异常，由全局异常处理器来处理响应信息
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), msg, GatewayErrorCode.PARAM_CHECK_ERROR);
        }
    }

    public String requestPathValid(String requestPath){
        if(StringUtil.isEmpty(requestPath) || "/".equals(requestPath.trim())){
            return "请求路径不能为空";
        }else{
            return "";
        }
    }

    public String textParamValid(RequestParam requestParam, String path){
        if(requestParam == null){
            return "请求体为空！";
        }else if(StringUtil.isEmpty(requestParam.getMethod())){
            return "method 为空！";
        }else if(StringUtil.isEmpty(requestParam.getVersion())){
            return "version 为空！";
        } else if (requestParam.getData() == null || StringUtil.isEmpty(requestParam.getData().toString())) {
            return "data 为空！";
        } else if (StringUtil.isEmpty(requestParam.getRandStr())) {
            return "randStr 为空！";
        } else if (StringUtil.isEmpty(requestParam.getSignType())) {
            return "signType 为空！";
        } else if (StringUtil.isEmpty(requestParam.getMchNo())) {
            return "mchNo 为空！";
        } else if(StringUtil.isEmpty(requestParam.getTimestamp())){
            return "timestamp 为空！";
        } else if (StringUtil.isEmpty(requestParam.getSignature())) {
            return "signature 签名串为空！";
        }

        if(StringUtil.isLengthOver(requestParam.getMethod(), 64)){
            return "method 的长度不能超过64！";
        }else if(StringUtil.isLengthOver(requestParam.getVersion(), 5)){
            return "version 的长度不能超过5！";
        }else if(! StringUtil.isLengthOk(requestParam.getRandStr(), 32,32)){
            return "randStr 的长度须为32！";
        }else if(StringUtil.isLengthOver(requestParam.getSignType(), 5)){
            return "signType 的长度不能超过5！";
        }else if(! StringUtil.isLengthOk(requestParam.getMchNo(), 10, 15)){
            return "mchNo 的长度须在10~15之间！";
        }else if(! ValidateUtil.isNumericOnly(requestParam.getTimestamp())){
            return "timestamp 须为纯数字！";
        }else if(StringUtil.isLengthOver(requestParam.getTimestamp(), 18)){
            return "timestamp 的长度不能超过18！";
        }

        long timestamp = Long.valueOf(requestParam.getTimestamp());
        long timeDiff = System.currentTimeMillis() - timestamp;
        if(timeDiff < 0) {
            return "timestamp 时间超前！";
        }else if(timeDiff > gatewayProperties.getRequestExpire()){
            return "timestamp 请求已过期！";
        } else if (!ValidateUtil.isInteger(requestParam.getSignType()) || SignTypeEnum.getEnum(Integer.parseInt(requestParam.getSignType())) == null) {
            return "signType 非法参数值 !!";
        }

        String allowMethods = gatewayProperties.getPathConf().get(path) == null ? "" : gatewayProperties.getPathConf().get(path).getAllowMethods();
        if(StringUtil.isEmpty(allowMethods)){
            return "当前path不允许访问";
        }else{
            String[] methodArr = allowMethods.split(",");
            if(! Arrays.asList(methodArr).contains(requestParam.getMethod())){
                return "method 非法参数值 !!";
            }
        }
        return "";
    }

    public String fileParamValid(FileUploadParam uploadParam, String path){
        if(uploadParam == null){
            return "请求体为空！";
        }else if(StringUtil.isEmpty(uploadParam.getMethod())){
            return "method 为空！";
        }else if(StringUtil.isEmpty(uploadParam.getVersion())){
            return "version 为空！";
        } else if (StringUtil.isEmpty(uploadParam.getRandStr())) {
            return "randStr 为空！";
        } else if (StringUtil.isEmpty(uploadParam.getSignType())) {
            return "signType 为空！";
        } else if (StringUtil.isEmpty(uploadParam.getMchNo())) {
            return "mchNo 为空！";
        } else if(StringUtil.isEmpty(uploadParam.getTimestamp())){
            return "timestamp 为空！";
        } else if(StringUtil.isEmpty(uploadParam.getHash())){
            return "hash 为空！";
        } else if(uploadParam.getFiles() == null || uploadParam.getFiles().size() == 0){
            return "上传的文件为空！";
        } else if (StringUtil.isEmpty(uploadParam.getSignature())) {
            return "signature 签名串为空！";
        }

        if(StringUtil.isLengthOver(uploadParam.getMethod(), 64)){
            return "method 的长度不能超过64！";
        }else if(StringUtil.isLengthOver(uploadParam.getVersion(), 5)){
            return "version 的长度不能超过5！";
        }else if(! StringUtil.isLengthOk(uploadParam.getRandStr(), 32,32)){
            return "randStr 的长度须为32！";
        }else if(StringUtil.isLengthOver(uploadParam.getSignType(), 5)){
            return "signType 的长度不能超过5！";
        }else if(! StringUtil.isLengthOk(uploadParam.getMchNo(), 10, 15)){
            return "mchNo 的长度须在10~15之间！";
        }else if(! ValidateUtil.isNumericOnly(uploadParam.getTimestamp())){
            return "timestamp 须为纯数字！";
        }else if(StringUtil.isLengthOver(uploadParam.getTimestamp(), 18)){
            return "timestamp 的长度不能超过18！";
        }

        long timestamp = Long.valueOf(uploadParam.getTimestamp());
        long timeDiff = System.currentTimeMillis() - timestamp;
        if(timeDiff < 0) {
            return "timestamp 时间超前！";
        }else if(timeDiff > gatewayProperties.getRequestExpire()){
            return "timestamp 请求已过期！";
        } else if (!ValidateUtil.isInteger(uploadParam.getSignType()) || SignTypeEnum.getEnum(Integer.parseInt(uploadParam.getSignType())) == null) {
            return "signType 非法参数值 !!";
        }

        String allowMethods = gatewayProperties.getPathConf().get(path) == null ? "" : gatewayProperties.getPathConf().get(path).getAllowMethods();
        if(StringUtil.isEmpty(allowMethods)){
            return "当前path不允许访问";
        }else{
            String[] methodArr = allowMethods.split(",");
            if(! Arrays.asList(methodArr).contains(uploadParam.getMethod())){
                return "method 非法参数值 !!";
            }
        }
        return "";
    }
}
