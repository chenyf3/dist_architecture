package com.xpay.common.api.webflux;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.dto.RequestDto;
import com.xpay.common.api.dto.ResponseDto;
import com.xpay.common.api.exceptions.ApiException;
import com.xpay.common.api.utils.SentinelBlockUtil;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.statics.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用webflux时的全局异常处理器，负责决定返回什么响应体
 */
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable ex = getError(request);
        RequestDto requestDto = getRequestDto(request);
        ResponseDto response = getResponseDto(request, requestDto, ex);

        Map<String, Object> map = new HashMap<>();
        Field[] fields = ResponseDto.class.getDeclaredFields();
        for(int i=0; i<fields.length; i++){
            Field filed = fields[i];
            String name = filed.getName();

            filed.setAccessible(true);
            try{
                map.put(name, filed.get(response));
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    private RequestDto getRequestDto(ServerRequest request){
        String mchNo = request.headers().firstHeader(HttpHeaderKey.REQUEST_MCH_NO_KEY);
        String signType = request.headers().firstHeader(HttpHeaderKey.REQUEST_SIGN_TYPE_KEY);

        RequestDto requestDto = new RequestDto();
        requestDto.setMchNo(mchNo==null ? "" : mchNo);
        requestDto.setSignType(signType == null ? "" : signType);
        return requestDto;
    }

    private ResponseDto getResponseDto(ServerRequest request, RequestDto requestDto, Throwable ex){
        String mchNo = requestDto.getMchNo();
        String signType = requestDto.getSignType();
        ResponseDto response;

        if (ex instanceof ApiException) {
            ApiException e = (ApiException) ex;
            logger.error("业务异常，path={} mchNo={} respCode={} errorMsg={}", request.path(), mchNo, e.getRespCode(), e.getRespMsg());
            response = new ResponseDto();
            response.setRespCode(e.getRespCode());
            response.setRespMsgWithCodeAndMsg(e.getRespCode(), e.getRespMsg());
            response.unknownIfEmpty();
        } else if (ex instanceof BizException) {
            BizException e = (BizException) ex;
            logger.error("业务异常，path={} mchNo={} code={} msg={}", request.path(), mchNo, e.getCode(), e.getMsg());
            response = new ResponseDto();
            response.unknownIfEmpty();
        } else if (ex instanceof ResponseStatusException) {
            logger.error("请求路径或请求数据有误，path={} mchNo={} errorMsg={}", request.path(), mchNo, ex.getMessage());
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            response = new ResponseDto();
            response.setRespCode(ApiRespCodeEnum.PARAM_FAIL.getValue());
            response.setRespMsg(responseStatusException.getReason());
        } else if (SentinelBlockUtil.isBlockException(ex)) {
            /**
             * 关于webflux和webmvc对于sentinel抛出 BlockException 的不同处理方式，请参考 {@link com.xpay.common.api.config.SentinelMvcAutoConfiguration} 上的注释说明
             */
            logger.error("出现流控异常，path={} mchNo={} Exception={}", request.path(), mchNo, SentinelBlockUtil.getBlockMsg(ex));
            response = new ResponseDto();
            response.setRespCode(ApiRespCodeEnum.SYS_FORBID.getValue());
            response.setRespMsg("System Limiting!");
        } else {
            logger.error("出现未预期的异常，path={} mchNo={}", request.path(), mchNo, ex);
            response = ResponseDto.unknown(mchNo, signType);
            response.setRespMsg("Internal Server Error");
        }

        response.setMchNo(mchNo);
        response.setSignType(signType);
        return response;
    }
}
