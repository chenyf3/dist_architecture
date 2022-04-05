package com.xpay.web.api.common.interceptor;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.StringUtil;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.config.WebApiProperties;
import com.xpay.web.api.common.manager.FuncManager;
import com.xpay.web.api.common.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 权限校验拦截器，此拦截器需要在Token拦截器后面，不然无法获取登录名、商户编号等信息
 */
public class AuthorityInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private FuncManager funcManager;
    private WebApiProperties properties;

    public AuthorityInterceptor(FuncManager funcManager, WebApiProperties properties){
        this.funcManager = funcManager;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        if (WebUtil.isWhiteList(request, properties.getWhiteListPrefix(), properties.getWhiteListSuffix())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            logger.info("被识别为非HandlerMethod uri={} handler={}", request.getRequestURI(), handler.getClass().getName());
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Permission permission = handlerMethod.getMethodAnnotation(Permission.class);
        if(permission == null){ //说明不需要权限控制
            return true;
        }

        String permissionFlag = permission.value();
        String loginName = (String) request.getAttribute(Constants.REQUEST_USER_LOGIN_NAME);
        String mchNo = (String) request.getAttribute(Constants.REQUEST_USER_MCH_NO);
        Map<String, String> authMap = funcManager.getAuthMapByLoginName(loginName, mchNo);

        boolean isPermit = false;
        if(permissionFlag.contains("|")){ //或逻辑，只要满足一个即可
            String[] permissionArr = permissionFlag.split("\\|");
            for(int i=0; i<permissionArr.length; i++){
                String permit = permissionArr[i].trim();
                if(StringUtil.isEmpty(permit)){//不允许写 ||
                    isPermit = false;
                    break;
                }else{
                    isPermit = authMap.containsKey(permit + "");
                }
                if(isPermit){
                    break;
                }
            }
        }else if(permissionFlag.contains("&")){ //与逻辑，必须全部满足
            String[] permissionArr = permissionFlag.split("\\&");
            for(int i=0; i<permissionArr.length; i++){
                String permit = permissionArr[i].trim();
                if(StringUtil.isEmpty(permit)){//不允许写 &&
                    isPermit = false;
                    break;
                }else{
                    isPermit = authMap.containsKey(permit + "");
                }
                if(! isPermit){
                    break;
                }
            }
        }else{
            isPermit = authMap.containsKey(permissionFlag + "");//强转字面量string，避免对象比较不匹配
        }

        if(isPermit){
            return true;
        }else{
            logger.warn("uri={} loginName={} permissionFlag={} authMap.size={} 无访问权限", request.getRequestURI(), loginName, permissionFlag, authMap==null?"":authMap.size());
            throw new BizException(RestResult.PERMISSION_DENY, "无访问权限！");
        }
    }
}

