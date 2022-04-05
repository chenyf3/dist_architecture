package com.xpay.starter.monitor.filter;

import com.xpay.starter.monitor.util.MonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 监控安全控制拦截器
 * @author chenyf
 * @date 20-12-3
 */
public class MvcManagementEndpointSecurityFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String tokenHeader;
    private final List<String> permitTokens;

    public MvcManagementEndpointSecurityFilter(String tokenHeader, String permitTokens){
        if(tokenHeader == null || tokenHeader.trim().length() == 0){
            throw new IllegalArgumentException("tokenHeader不能为空");
        }else if(permitTokens == null || permitTokens.trim().length() == 0){
            throw new IllegalArgumentException("permitTokens不能为空");
        }
        this.tokenHeader = tokenHeader;
        this.permitTokens = Arrays.asList(permitTokens.split(","));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String token = request.getHeader(tokenHeader);
        if(token == null || token.trim().length() == 0){
            logger.warn("FORBIDDEN ip={} path={} token={}", MonitorUtil.getIpAddr(request), request.getRequestURI(), token);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        if(permitTokens.contains(token)){
            chain.doFilter(request, response);
            return;
        }

        //兼容http basic认证的方式
        String[] tokenArr = token.trim().split("Bearer ");
        if(tokenArr.length > 1 && permitTokens.contains(tokenArr[1])){
            chain.doFilter(request, response);
            return;
        }

        logger.warn("UNAUTHORIZED ip={} path={} token={}", MonitorUtil.getIpAddr(request), request.getRequestURI(), token);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
}
