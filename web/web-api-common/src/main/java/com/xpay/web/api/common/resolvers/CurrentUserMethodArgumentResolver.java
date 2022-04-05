package com.xpay.web.api.common.resolvers;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.StringUtil;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.UserService;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 增加方法注入，将含有CurrentUser注解的方法参数注入当前登录用户
 * @author Derek
 * @date 2018/04/09
 */
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {
	UserService userService;

	public CurrentUserMethodArgumentResolver(UserService userService){
	    this.userService = userService;
    }
	
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //如果参数类型是User并且有CurrentUser注解则支持
        if (parameter.getParameterType().isAssignableFrom(UserModel.class) &&
                parameter.hasParameterAnnotation(CurrentUser.class)) {
            return true;
        }
        return false;
    }

	@Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //取出鉴权时存入的登录用户Id
        String loginName = (String)webRequest.getAttribute(Constants.REQUEST_USER_LOGIN_NAME, RequestAttributes.SCOPE_REQUEST);
        String mchNo = (String) webRequest.getAttribute(Constants.REQUEST_USER_MCH_NO, RequestAttributes.SCOPE_REQUEST);
        String requestIp = (String)webRequest.getAttribute(Constants.REQUEST_USER_IP, RequestAttributes.SCOPE_REQUEST);
        if (StringUtil.isNotEmpty(loginName)) {
            UserModel userModel = userService.getUserModelByLoginNameCache(loginName, mchNo);
        	if (userModel != null) {
                userModel.setRequestIp(requestIp);
				return userModel;
			}
        }
        throw new BizException(RestResult.BIZ_ERROR, "用户不存在");
    }
}
