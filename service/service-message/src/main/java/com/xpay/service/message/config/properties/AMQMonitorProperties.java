package com.xpay.service.message.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.activemq.monitor")
public class AMQMonitorProperties {
    //是否开启监控
    private boolean enabled = true;
    //整个集群公用的用户名
    private String username;
    //整个集群公用的密码
    private String password;
    //整个集群公用的端口号
    private int port = 8161;
    //整个集群公用的间隔时间
    private int interval = 30;
    //被监控的Broker节点
    public List<Node> nodes;
    //忽略监控的队列
    private String omitQueues = "";
    //队列的默认阻塞阈值
    private Integer defaultBlockSize = 100;
    //配置队列的阻塞阈值
    private Map<String, Integer> blockSize = new HashMap<>();

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getOmitQueues() {
        return omitQueues;
    }

    public void setOmitQueues(String omitQueues) {
        this.omitQueues = omitQueues;
    }

    public Integer getDefaultBlockSize() {
        return defaultBlockSize;
    }

    public void setDefaultBlockSize(Integer defaultBlockSize) {
        this.defaultBlockSize = defaultBlockSize;
    }

    public Map<String, Integer> getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Map<String, Integer> blockSize) {
        this.blockSize = blockSize;
    }

    public static class Node {
        private String host;
        private Integer port;
        private String brokerName;
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public void setBrokerName(String brokerName) {
            this.brokerName = brokerName;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }
}
