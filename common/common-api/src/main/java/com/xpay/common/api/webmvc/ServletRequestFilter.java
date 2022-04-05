package com.xpay.common.api.webmvc;

import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * SpringMVC的请求拦截器
 * @author chenyf
 * @date 2018-12-15
 */
public class ServletRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //设置 ServletRequest、ServletResponse 的包装类
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        chain.doFilter(requestWrapper, response);
    }
}
