package com.xpay.common.api.webmvc;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.dto.RequestDto;
import com.xpay.common.api.dto.ResponseDto;
import com.xpay.common.api.exceptions.ApiException;
import com.xpay.common.api.utils.SentinelBlockUtil;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.statics.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.RequestDispatcher;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用webmvc时的全局异常处理器，负责决定返回什么响应体
 */
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest request, ErrorAttributeOptions options) {
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

    private RequestDto getRequestDto(WebRequest request){
        String mchNo = request.getHeader(HttpHeaderKey.REQUEST_MCH_NO_KEY);
        String signType = request.getHeader(HttpHeaderKey.REQUEST_SIGN_TYPE_KEY);

        RequestDto requestDto = new RequestDto();
        requestDto.setMchNo(mchNo==null ? "" : mchNo);
        requestDto.setSignType(signType==null ? "" : signType);
        return requestDto;
    }

    private ResponseDto getResponseDto(WebRequest request, RequestDto requestDto, Throwable ex){
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI, RequestAttributes.SCOPE_REQUEST);
        String mchNo = requestDto.getMchNo();
        String signType = requestDto.getSignType();
        ResponseDto response;

        StringBuilder errMsg = new StringBuilder();
        if (ex instanceof ApiException) {
            ApiException e = (ApiException) ex;
            logger.error("业务异常，path={} mchNo={} respCode={} errorMsg={}", path, mchNo, e.getRespCode(), e.getRespMsg());
            response = new ResponseDto();
            response.setRespCode(e.getRespCode());
            response.setRespMsgWithCodeAndMsg(e.getRespCode(), e.getRespMsg());
            response.unknownIfEmpty();//如果业务方没有设置，则设置受理未知
        } else if (ex instanceof BizException) {
            BizException e = (BizException) ex;
            logger.error("业务异常，path={} mchNo={} code={} msg={}", path, mchNo, e.getCode(), e.getMsg());
            response = new ResponseDto();
            response.unknownIfEmpty();//如果业务方没有设置，则设置受理未知
        } else if(isRequestFrameworkException(ex, errMsg)){
            logger.error("请求路径或请求数据有误，path={} mchNo={} errorMsg={}", path, mchNo, ex.getMessage());
            response = new ResponseDto();
            response.setRespCode(ApiRespCodeEnum.PARAM_FAIL.getValue());
            response.setRespMsg(errMsg.toString());
        } else if(SentinelBlockUtil.isBlockException(ex)) {
            logger.error("出现流控异常，path={} mchNo={} Exception={}", path, mchNo, SentinelBlockUtil.getBlockMsg(ex));
            response = ResponseDto.unknown(mchNo, signType);
            response.setRespMsg("System Limiting!");
        } else {
            logger.error("出现未预期的异常，path={} mchNo={}", path, mchNo, ex);
            response = ResponseDto.unknown(mchNo, signType);
            response.setRespMsg("Internal Server Error");
        }

        response.setMchNo(mchNo);
        response.setSignType(signType);
        return response;
    }

    private boolean isRequestFrameworkException(Throwable ex, StringBuilder strBuilder){
        if (ex instanceof NoHandlerFoundException) { //path路径不存在时
            strBuilder.append("请求路径错误，请检查method参数");
            return true;
        } else if (ex instanceof MethodArgumentNotValidException) { //hibernate-validator 参数校验不通过时
            FieldError fieldError = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldError();
            String field = fieldError.getField();
            String msg = fieldError.getDefaultMessage();
            strBuilder.append("验参失败，"+field+":"+msg+"");
            return true;
        } else if (ex instanceof HttpMessageNotReadableException) { //参数类型转换错误时
            strBuilder.append("请求参数无法转换或读取，请检查参数类型");
            return true;
        } else if (ex instanceof HttpMediaTypeNotSupportedException) { //用户传入的MediaType与系统在方法上设置的不一致时
            strBuilder.append("请参照接口文档选择合适的请求MediaType");
            return true;
        } else if (ex instanceof HttpRequestMethodNotSupportedException) { //用户请求方式与系统在方法上设置的不一致时，如：方法要求POST但用法使用GET请求
            strBuilder.append("请参照接口文档选择合适的HTTP Method");
            return true;
        } else if (ex instanceof HttpMediaTypeNotAcceptableException) {
            strBuilder.append("请参照接口文档选择合适的响应MediaType");
            return true;
        } else if (ex instanceof MissingPathVariableException) {
            strBuilder.append("请参照接口文档传入合适的uri参数");
            return true;
        } else if (ex instanceof MissingServletRequestParameterException) {
            strBuilder.append("请参照接口文档传入合适的请求参数");
            return true;
        } else if (ex instanceof ConversionNotSupportedException) {
            strBuilder.append("参数类型错误，请详细阅读相关接口文档");
            return true;
        } else if (ex instanceof TypeMismatchException) {
            strBuilder.append("参数类型错误，请详细阅读相关接口文档");
            return true;
        } else if (ex instanceof MissingServletRequestPartException) {
            strBuilder.append("系统不支持form-data或请求体内容有误");
            return true;
        }
        return false;
    }
}
