package com.xpay.starter.sentinel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.sentinel.client")
public class ClientProperties {
    /**
     * 是否启用客户端配置
     */
    private Boolean enabled = Boolean.TRUE;
    /**
     * 应用名称
     */
    @Value("${spring.application.name}")
    private String appName;
    /**
     * 流控规则服务器配置
     */
    private RuleServer ruleServer = new RuleServer();
    /**
     * TokenServer的地址获取其注册中心，用以获取TokenServer服务提供者
     */
    private TokenServer tokenServer = new TokenServer();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public RuleServer getRuleServer() {
        return ruleServer;
    }

    public void setRuleServer(RuleServer ruleServer) {
        this.ruleServer = ruleServer;
    }

    public TokenServer getTokenServer() {
        return tokenServer;
    }

    public void setTokenServer(TokenServer tokenServer) {
        this.tokenServer = tokenServer;
    }

    /**
     * 当前应用流控规则的配置中心地址
     */
    public static class RuleServer {
        private String serverAddr;
        private String namespace;
        private String username;
        private String password;
        private String groupId = "SENTINEL_GROUP"; // 配置文件的分组

        public String getServerAddr() {
            return serverAddr;
        }

        public void setServerAddr(String serverAddr) {
            this.serverAddr = serverAddr;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
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

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }

    /**
     * TokenServer的服务注册地址
     */
    public static class TokenServer {
        private Integer requestTimeout = 10;//TokenServer 的请求超时时间(毫秒)

        //直接指定TokenServer的方式时，请配置这两个内容
        private String serverHost;//TokenServer 的地址
        private Integer serverPort;//TokenServer 的端口

        //使用服务注册发现的方式时，则配置如下内容
        private String serverAddr;
        private String namespace;
        private String username;
        private String password;
        private String groupId = "SENTINEL_GROUP"; // TokenServer在注册中心的分组
        private String serviceName = "sentinel-token-server";//TokenServer在注册中心的服务名
        private String clusterName = "DEFAULT";//TokenServer在注册中心的集群名

        public String getServerHost() {
            return serverHost;
        }

        public void setServerHost(String serverHost) {
            this.serverHost = serverHost;
        }

        public Integer getServerPort() {
            return serverPort;
        }

        public void setServerPort(Integer serverPort) {
            this.serverPort = serverPort;
        }

        public Integer getRequestTimeout() {
            return requestTimeout;
        }

        public void setRequestTimeout(Integer requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public String getServerAddr() {
            return serverAddr;
        }

        public void setServerAddr(String serverAddr) {
            this.serverAddr = serverAddr;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
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

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }
    }
}
