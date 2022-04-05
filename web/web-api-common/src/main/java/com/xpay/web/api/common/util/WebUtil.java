package com.xpay.web.api.common.util;

import com.xpay.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Derek
 * @Created 2018/4/9
 * @Description
 **/
public class WebUtil {
    private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);
    private static final String HEADER_X_FORWARD = "x-forwarded-for";
    private static final String HEADER_PROXY_CLIENT = "Proxy-Client-IP";
    private static final String HEADER_WL_PROXY_CLIENT = "WL-Proxy-Client-IP";
    private static final String HEADER_UNKNOWN_VALUE = "unknown";
    private static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCAL_IPV4 = "127.0.0.1";

    /**
     * 获取客户端的IP地址
     *
     * @param request 请求参数
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader(HEADER_X_FORWARD);
        if (StringUtil.isEmpty(ipAddress) || HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(HEADER_PROXY_CLIENT);
        }
        if (StringUtil.isEmpty(ipAddress) || HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(HEADER_WL_PROXY_CLIENT);
        }
        if (StringUtil.isEmpty(ipAddress) || HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (StringUtil.isNotEmpty(ipAddress) && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        if(LOCAL_IPV6.equals(ipAddress) || StringUtil.isEmpty(ipAddress)){
            ipAddress = LOCAL_IPV4;
        }
        return ipAddress;
    }

    public static boolean isWhiteList(HttpServletRequest request, String whiteListPrefix, String whiteListSuffix){
        String uri = request.getRequestURI();
        if("/error".equals(uri) || "/".equals(uri)){
            return true;
        }

        //判断是否白名单路径，如果是，则直接返回true
        if(StringUtil.isNotEmpty(whiteListPrefix)){
            String[] arr = whiteListPrefix.split(",");
            for(int i=0; i<arr.length; i++){
                if(uri.startsWith(arr[i])){
                    return true;
                }
            }
        }

        //判断是否要访问静态文件，如果是，则直接返回true
        String suffix = uri.substring(uri.lastIndexOf(".") + 1);
        if(StringUtil.isNotEmpty(suffix) && StringUtil.isNotEmpty(whiteListSuffix)){
            List<String> suffixList = Arrays.asList(whiteListSuffix.split(","));
            if(suffixList.contains(suffix)){
                return true;
            }
        }
        return false;
    }
}
