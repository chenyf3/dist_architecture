package com.xpay.web.api.common.resolvers;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.web.api.common.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class CustomizeHandlerExceptionResolver extends DefaultHandlerExceptionResolver implements Ordered {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int getOrder(){
        return HIGHEST_PRECEDENCE;
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,
                                              @Nullable Object handler, Exception ex){
        int httpStatus = 200;
        Integer errCode = null;
        String errorMsg = null;
        try{
            logger.error("发生异常", ex);
            if(ex instanceof BizException){
                httpStatus = HttpServletResponse.SC_OK;
                errorMsg = ((BizException) ex).getMsg();
                errCode = ((BizException) ex).getCode();
                if(! RestResult.isRestRespCode(errCode)){
                    errCode = RestResult.BIZ_ERROR;//统一设置为业务错误
                }
            }else if (ex instanceof HttpRequestMethodNotSupportedException) {
                httpStatus = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
                errorMsg = "MethodNotSupported";
            }
            else if (ex instanceof HttpMediaTypeNotSupportedException) {
                httpStatus = HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
                errorMsg = "MediaTypeNotSupported";
            }
            else if (ex instanceof HttpMediaTypeNotAcceptableException) {
                httpStatus = HttpServletResponse.SC_NOT_ACCEPTABLE;
                errorMsg = "MediaTypeNotAcceptable";
            }
            else if (ex instanceof MissingPathVariableException) {
                httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                errorMsg = "PathVariableMissing";
            }
            else if (ex instanceof MissingServletRequestParameterException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                errorMsg = "RequestParameterMissing";
            }
            else if (ex instanceof ServletRequestBindingException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                errorMsg = "RequestBindFail";
            }
            else if (ex instanceof ConversionNotSupportedException) {
                httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                errorMsg = "ConversionNotSupported";
            }
            else if (ex instanceof TypeMismatchException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                errorMsg = "TypeMismatch";
            }
            else if (ex instanceof HttpMessageNotReadableException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                errorMsg = "NotReadable";
            }
            else if (ex instanceof HttpMessageNotWritableException) {
                httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                errorMsg = "NotWritable";
            }
            else if (ex instanceof MethodArgumentNotValidException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
                List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
                if(fieldErrorList == null || fieldErrorList.isEmpty()){
                    errorMsg = "MethodNotSupported";
                }else{
                    errorMsg = fieldErrorList.get(0).getDefaultMessage();
                }
            }
            else if (ex instanceof MissingServletRequestPartException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                errorMsg = "MissingRequestPart";
            }
            else if (ex instanceof BindException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                List<FieldError> fieldErrorList = ((BindException) ex).getFieldErrors();
                if(fieldErrorList == null || fieldErrorList.isEmpty()){
                    errorMsg = "BindFail";
                }else{
                    errorMsg = fieldErrorList.get(0).getDefaultMessage();
                }
            }
            else if (ex instanceof NoHandlerFoundException) {
                httpStatus = HttpServletResponse.SC_NOT_FOUND;
                errorMsg = "NoHandlerFound";
            }
            else if (ex instanceof AsyncRequestTimeoutException) {
                httpStatus = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                errorMsg = "RequestTimeout";
            }
            else if (ex instanceof MaxUploadSizeExceededException) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                // byte换算成Mb
                errorMsg = "文件大小不得超过" + ((MaxUploadSizeExceededException) ex).getMaxUploadSize()/(1024*1024) + "Mb";
            }
            else{
                httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                errorMsg = "系统异常，请联系客服或系统管理员";
                logger.error("出现未预期异常", ex);
            }
        }catch(Throwable e){
            errorMsg = "ExceptionResolveError";
            logger.error("处理异常情况时出现异常，ex={}", ex.getMessage(), e);
        }

        try{
            request.setAttribute(Constants.REQUEST_EXCEPTION_CODE, errCode);
            request.setAttribute(Constants.REQUEST_EXCEPTION_MSG, errorMsg);
            if(! response.isCommitted()){
                response.sendError(httpStatus);
            }
            return new ModelAndView();
        }catch(Exception e){
            logger.error("设置响应信息时出现异常", e);
            return null;
        }
    }

    @Override
    protected void logException(Exception ex, HttpServletRequest request) {
        if(ex instanceof BizException){
            return;
        }else{
            if(logger.isWarnEnabled()){
                logger.warn(super.buildLogMessage(ex, request));
            }
        }
    }
}
