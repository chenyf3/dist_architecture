package com.xpay.web.api.common.error.handler;

import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.web.api.common.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * 使用webmvc时的全局异常处理器，负责决定返回什么响应体
 */
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest request, ErrorAttributeOptions options) {
        Object errCode = request.getAttribute(Constants.REQUEST_EXCEPTION_CODE, RequestAttributes.SCOPE_REQUEST);
        String errMsg = (String) request.getAttribute(Constants.REQUEST_EXCEPTION_MSG, RequestAttributes.SCOPE_REQUEST);
        if(StringUtil.isEmpty(errMsg)){
            Throwable ex = getError(request);
            if(ex != null){
                logger.error("未预期的系统异常 Exception = {}", ex.getMessage());
            }
            errMsg = "系统异常";
        }
        RestResult<?> responseDto = RestResult.error(errMsg);
        if(errCode != null){
            responseDto.setCode((Integer) errCode);
        }
        return BeanUtil.toMap(responseDto);
    }
}
