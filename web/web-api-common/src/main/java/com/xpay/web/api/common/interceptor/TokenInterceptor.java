package com.xpay.web.api.common.interceptor;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.StringUtil;
import com.xpay.web.api.common.manager.TokenManager;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.config.WebApiProperties;
import com.xpay.web.api.common.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * token校验拦截器，校验token是否存在且有效，若校验通过，会往HttpServletRequest设置登录名和商户编号等信息
 * @author Derek
 * @date 2018/04/09
 */
public class TokenInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebApiProperties properties;
    private TokenManager tokenManager;

	public TokenInterceptor(TokenManager tokenManager, WebApiProperties properties){
	    this.tokenManager = tokenManager;
        this.properties = properties;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if("OPTIONS".equalsIgnoreCase(request.getMethod())){
            return false;
        }else if (WebUtil.isWhiteList(request, properties.getWhiteListPrefix(), properties.getWhiteListSuffix())) {
            return true;
        }
        //1.获取token
        String token = getToken(request);

        //2.验证token
        Map<String, String> claims = tokenManager.verifyAndRenewToken(token);
        if (claims == null) {
            throw new BizException(RestResult.TOKEN_INVALID, "登录凭证无效或已过期，请重新登录");
        }

        //3.验证token和客户端信息是否匹配
        String clientIp = WebUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        if ( !tokenManager.validateClientInfo(claims, clientIp, userAgent) ) {
            throw new BizException(RestResult.TOKEN_INVALID, "客户端信息不匹配");
        }
        
        //4.如果token验证成功，将token中存储的登录名、商户号存到request中，便以在后续的流程中获取
        String loginName = tokenManager.getLoginName(claims);
        String mchNo = claims.get(TokenManager.CLAIM_KEY_MCH_NO);
        request.setAttribute(Constants.REQUEST_USER_LOGIN_NAME, loginName);
        request.setAttribute(Constants.REQUEST_USER_MCH_NO, mchNo);
        request.setAttribute(Constants.REQUEST_USER_IP, clientIp);
        return true;
    }

    /**
     * @return
     */
    private String getToken(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String methodType = request.getMethod();
//        String regEx = String.format("^%s.*$", properties.getDownloadPath());
//        Pattern pattern = Pattern.compile(regEx);
//        Matcher matcher = pattern.matcher(uri);
        String token = request.getHeader(Constants.HTTP_TOKEN_HEADER);
        if (StringUtil.isEmpty(token)) {
            token = request.getParameter("token");
        }
        if(StringUtil.isEmpty(token)) {
            logger.error("uri={} methodType={} Token不能为空", uri, methodType);
            throw new BizException(RestResult.TOKEN_INVALID, "Token不能为空");
        }
        return token;
    }
}
