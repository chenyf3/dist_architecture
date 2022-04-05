package com.xpay.gateway.callback.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetSocketAddress;

/**
 * IP获取工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class IPUtil {
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String HEADER_X_FORWARD_FOR = "X-Forwarded-For";
    private static final String HEADER_PROXY_CLIENT = "Proxy-Client-IP";//使用apache http服务器做代理时会带上的请求头
    private static final String HEADER_WL_PROXY_CLIENT = "WL-Proxy-Client-IP";//apache http服务器的webLogic插件加上的头
    private static final String HEADER_UNKNOWN_VALUE = "unknown";
    private static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCAL_IPV4 = "127.0.0.1";

    /**
     * 获取 X-Real-IP 请求头的值，为避免来源ip伪造，此功能要求服务端必须有代理并且按照如下配置：
     *  1.服务端最外层nginx代理需设置： proxy_set_header X-Real-Ip $remote_addr;  表示把remote_addr的值设置到X-Real-Ip请求头
     *  2.如果服务端有多层代理，则除最外层外的其他代理都需要设置：proxy_set_header X-Real-IP $http_x_real_ip;  表示把X-Real-IP请求头的值一直往下传递
     *
     * 补充说明：
     *  remote_addr一般不会被伪造，它表示与服务端建立tcp连接的那台客户端机器的ip，如果客户端没有使用代理，则代表客户端的真实IP，如果客户端有使用代理，
     *  则这个值是客户端最外层代理机器的ip
     *
     * @param request
     * @return
     */
    public static String getXRealIpAddress(ServerHttpRequest request) {
        String ipAddr = request.getHeaders().getFirst(HEADER_X_REAL_IP);
        if (isValidIp(ipAddr)) {
            return ipAddr;
        }

        //如果X-Real-Ip中没有取到合适的ip，则直接取remote_addr的值(这种情况可能在开发和测试环境中会比较常见)
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "";
    }

    /**
     * 获取客户端的IP地址（此方法有可能获取到被伪造的来源ip，但会尽可能获取到客户端的ip，适合对ip来源校验要求不高的场景）
     * @param request 请求参数
     * @return
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ipAddress = headers.getFirst(HEADER_X_FORWARD_FOR);
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        ipAddress = headers.getFirst(HEADER_PROXY_CLIENT);
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        ipAddress = headers.getFirst(HEADER_WL_PROXY_CLIENT);
        if(isNotEmpty(ipAddress = getValidIp(ipAddress))) {
            return ipAddress;
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        ipAddress = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "";
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
