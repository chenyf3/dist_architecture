package com.xpay.starter.monitor.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求处理工具类，通过HttpServletRequest获取IP、请求数据实体等
 *
 * @author chenyf
 * @date 2018-12-15
 */
public class MonitorUtil {
    private static final String HEADER_X_FORWARD_FOR = "X-Forwarded-For";
    private static final String HEADER_PROXY_CLIENT = "Proxy-Client-IP";//使用apache http服务器做代理时会带上的请求头
    private static final String HEADER_WL_PROXY_CLIENT = "WL-Proxy-Client-IP";//apache http服务器的webLogic插件加上的头
    private static final String HEADER_UNKNOWN_VALUE = "unknown";
    private static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCAL_IPV4 = "127.0.0.1";

    /**
     * 获取客户端的IP地址（此方法有可能获取到被伪造的来源ip，但会尽可能获取到客户端的ip，适合对ip来源校验要求不高的场景）
     * @param request 请求参数
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader(HEADER_X_FORWARD_FOR);
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        ipAddress = request.getHeader(HEADER_PROXY_CLIENT);
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        ipAddress = request.getHeader(HEADER_WL_PROXY_CLIENT);
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        ipAddress = request.getRemoteAddr();
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        return "";
    }

    private static String getValidIp(String ip){
        if(!isValidIp(ip)) {
            return "";
        }

        //有多层代理时取左边第一个有效的ip
        String[] ipAddrArray = ip.split(",");
        for (String ipAddr : ipAddrArray) {
            if (isValidIp(ipAddr)) {
                return ipAddr.trim();
            }
        }
        return "";
    }

    private static boolean isValidIp(String ip) {
        return isNotEmpty(ip)
                && !HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ip)
                && !LOCAL_IPV6.equals(ip)
                && !LOCAL_IPV4.equals(ip);
    }

    public static boolean isEmpty(CharSequence sequence){
        return sequence == null || sequence.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence sequence){
        return !isEmpty(sequence);
    }
}
