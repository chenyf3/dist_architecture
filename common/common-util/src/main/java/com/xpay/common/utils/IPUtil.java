package com.xpay.common.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPUtil {
    private final static String INTERFACE_NAME = "team_jp";//生产环境配置网卡名

    public static String getIpAddress() {
        InetAddress inetAddr = getFirstInetAddress(INTERFACE_NAME);
        return inetAddr != null ? inetAddr.getHostAddress() : null;
    }

    public static String getHostName() {
        InetAddress inetAddr = getFirstInetAddress(INTERFACE_NAME);
        return inetAddr != null ? inetAddr.getHostName() : null;
    }

    public static String getLocalHost() {
        try{
            return InetAddress.getLocalHost().getHostName();
        }catch(Exception e){
            throw new RuntimeException("本地网络地址获取异常", e);
        }
    }

    public static String getLocalIp() {
        try{
            return InetAddress.getLocalHost().getHostAddress();
        }catch(Exception e){
            throw new RuntimeException("本地网络地址获取异常", e);
        }
    }

    private static InetAddress getFirstInetAddress(String interfaceName) {
        try{
            List<InetAddress> addressList = getInetAddress(interfaceName, true);
            if(addressList != null && addressList.size() > 0){
                return addressList.get(0);
            }
            return InetAddress.getLocalHost();
        }catch(Exception e){
            throw new RuntimeException("网络地址获取异常", e);
        }
    }

    private static List<InetAddress> getInetAddress(String interfaceName, boolean onlyGetFist) throws SocketException {
        List<InetAddress> addressList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isLoopback() || !ni.isUp()){ //跳过loopback和没有启用的网卡
                continue;
            }

            Enumeration<InetAddress> allAddress = ni.getInetAddresses();
            while (allAddress.hasMoreElements()) {
                InetAddress address = allAddress.nextElement();
                if (address.isLoopbackAddress() || address instanceof Inet6Address) {
                    continue; // 跳过loopback和IPv6地址
                }

                if (null == interfaceName) {
                    addressList.add(address);
                } else if (interfaceName.equals(ni.getName())) {
                    addressList.add(address);
                }else{
                    continue;
                }

                if(onlyGetFist){
                    return addressList;
                }
            }
        }
        return addressList;
    }

    public static void main(String[] args) throws Exception {
        getInetAddress(null, false);
    }
}
