package com.xpay.service.message.utils;

import com.xpay.service.message.config.properties.AMQMonitorProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ActiveMQUtil {

    public static boolean isActiveMQInnerDestination(String destination){
        return destination != null && destination.startsWith("ActiveMQ.");
    }

    public static boolean isVirtualTopic(String topic){
        return topic != null && topic.startsWith("VirtualTopic.");
    }

    public static String[] splitUrl(String brokerUrl){
        if(! brokerUrl.startsWith("failover")){
            throw new RuntimeException("当前ActiveMQ为非Failover模式，不处理。brokerUrl = " + brokerUrl);
        }
        int start = brokerUrl.indexOf("(") + 1;
        int end = brokerUrl.indexOf(")");
        String brokerUrls = brokerUrl.substring(start, end);
        String[] brokerArr = brokerUrls.split(",");
        if(brokerArr.length < 2){
            throw new RuntimeException("Failover模式下，Broker地址至少应有2个。brokerUrl = " + brokerUrl);
        }
        return brokerArr;
    }

    public static String[] splitHost(String brokerUrl){
        String[] brokerArr = splitUrl(brokerUrl);
        String[] brokerHost = new String[brokerArr.length];
        for(int i=0; i<brokerArr.length; i++){
            String addr = brokerArr[i]; //eg: addr = tcp://127.0.0.1:61616
            int begin = addr.indexOf("://")+3, end = addr.lastIndexOf(":");
            addr = addr.substring(begin, end);
            brokerHost[i] = addr;
        }
        return brokerHost;
    }

    public static String[] splitHostPort(String brokerUrl){
        String[] brokerArr = splitUrl(brokerUrl);
        String[] brokerHost = new String[brokerArr.length];
        for(int i=0; i<brokerArr.length; i++){
            String addr = brokerArr[i]; //eg: addr = tcp://127.0.0.1:61616
            int begin = addr.indexOf("://")+3;
            addr = addr.substring(begin);
            brokerHost[i] = addr;
        }
        return brokerHost;
    }

    public static Map<String, String> buildHeader(AMQMonitorProperties.Node brokerConf){
        String auth = brokerConf.getUsername() + ":" + brokerConf.getPassword();
        Map<String, String> header = new HashMap<>();
        header.put("Connection", "close");//http1.1中设置不使用长连接的方式，因为ActiveMQ服务端未必支持
        header.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
        return header;
    }
}
