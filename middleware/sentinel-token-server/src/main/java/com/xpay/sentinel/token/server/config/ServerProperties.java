package com.xpay.sentinel.token.server.config;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.xpay.sentinel.token.server.config.inet.InetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * TokenServer服务端配置属性
 * @author chenyf
 */
@ConfigurationProperties(prefix = "spring.cloud.sentinel.server")
public class ServerProperties {
    @Value("${spring.application.name:sentinel-token-server}")
    private String appName;
    private String ip;//当前实例的IP，如果不配置会自动获取本机IP
    private String networkInterface;//当前使用哪个网卡，在有多个网卡时使用
    private Integer port = 18730;//为 TokenClient 客户端提供服务的端口号
    private RuleServer rules = new RuleServer();
    private Registry registry = new Registry();
    private Transport transport = new Transport();

    @Autowired
    private InetUtils.InetUtilsProperties inetUtilsProperties;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public RuleServer getRules() {
        return rules;
    }

    public void setRules(RuleServer rules) {
        this.rules = rules;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    @PostConstruct
    public void init() throws Exception {
        InetUtils inetUtils = new InetUtils(inetUtilsProperties);
        if (StringUtils.isEmpty(ip)) {
            // traversing network interfaces if didn't specify a interface
            if (StringUtils.isEmpty(networkInterface)) {
                ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
            } else {
                NetworkInterface netInterface = NetworkInterface.getByName(networkInterface);
                if (null == netInterface) {
                    throw new IllegalArgumentException("no such interface " + networkInterface);
                }

                Enumeration<InetAddress> inetAddress = netInterface.getInetAddresses();
                while (inetAddress.hasMoreElements()) {
                    InetAddress currentAddress = inetAddress.nextElement();
                    if (currentAddress instanceof Inet4Address
                            && !currentAddress.isLoopbackAddress()) {
                        ip = currentAddress.getHostAddress();
                        break;
                    }
                }

                if (StringUtils.isEmpty(ip)) {
                    throw new RuntimeException("cannot find available ip from network interface " + networkInterface);
                }
            }
        }
        registry.setAppName(appName);
        registry.setIp(ip);
        registry.setPort(port);
        registry.getMetadata().put(PreservedMetadataKeys.REGISTER_SOURCE, "SPRING_CLOUD");
        registry.getMetadata().put(PreservedMetadataKeys.REGISTER_SOURCE, "SPRING_CLOUD");
        registry.getMetadata().put(PreservedMetadataKeys.HEART_BEAT_INTERVAL, registry.getHeartBeatInterval()+"");
        registry.getMetadata().put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT, registry.getHeartBeatTimeout()+"");
        registry.getMetadata().put(PreservedMetadataKeys.IP_DELETE_TIMEOUT, registry.getIpDeleteTimeout()+"");

        transport.setAppName(appName);
        transport.setClientIp(ip);
    }

    public static class RuleServer {
        private String serverAddr;
        private String namespace;
        private String username;
        private String password;
        private String groupId = "SENTINEL_GROUP"; // 默认分组
        private String namespaceSetDataId = "cluster-server-namespace-set";

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

        public String getNamespaceSetDataId() {
            return namespaceSetDataId;
        }

        public void setNamespaceSetDataId(String namespaceSetDataId) {
            this.namespaceSetDataId = namespaceSetDataId;
        }
    }

    public static class Registry {
        private String serverAddr;
        private String namespace;
        private String username;
        private String password;
        private String groupId = "SENTINEL_GROUP"; //默认分组
        /**
         * 往注册中心注册的应用名，无需配置，会自动被 {@link ServerProperties#appName} 覆盖
         */
        private String appName;
        /**
         * 当前实例的IP，无需配置，会自动被 {@link ServerProperties#ip} 覆盖
         */
        private String ip;
        /**
         * 当前Server对Client提供服务的端口，无需配置，会自动被 {@link ServerProperties#port} 覆盖
         */
        private Integer port;
        private float weight = 1;
        private Integer idleSeconds = 500;
        private String clusterName = "DEFAULT";
        private Map<String, String> metadata = new HashMap<>();
        /**
         * 和注册中心的心跳间隔，单位为毫秒
         */
        private Integer heartBeatInterval = 2000;
        /**
         * 服务端多少秒收不到客户端心跳，会将该客户端注册的实例设为不健康，单位为毫秒
         */
        private Integer heartBeatTimeout = 6000;
        /**
         * 服务端多少秒收不到客户端心跳，会将该客户端注册的实例删除(毫秒)
         */
        private Integer ipDeleteTimeout = 10000;

        /**
         * If instance is enabled to accept request. The default value is true.
         */
        private boolean instanceEnabled = true;

        /**
         * If instance is ephemeral.The default value is true.
         */
        private boolean ephemeral = true;
        /**
         * Throw exceptions during service registration if true, otherwise, log error
         * (defaults to true).
         */
        private boolean failFast = true;

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

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }

        public Integer getIdleSeconds() {
            return idleSeconds;
        }

        public void setIdleSeconds(Integer idleSeconds) {
            this.idleSeconds = idleSeconds;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        public Integer getHeartBeatInterval() {
            return heartBeatInterval;
        }

        public void setHeartBeatInterval(Integer heartBeatInterval) {
            this.heartBeatInterval = heartBeatInterval;
        }

        public Integer getHeartBeatTimeout() {
            return heartBeatTimeout;
        }

        public void setHeartBeatTimeout(Integer heartBeatTimeout) {
            this.heartBeatTimeout = heartBeatTimeout;
        }

        public Integer getIpDeleteTimeout() {
            return ipDeleteTimeout;
        }

        public void setIpDeleteTimeout(Integer ipDeleteTimeout) {
            this.ipDeleteTimeout = ipDeleteTimeout;
        }

        public boolean isInstanceEnabled() {
            return instanceEnabled;
        }

        public void setInstanceEnabled(boolean instanceEnabled) {
            this.instanceEnabled = instanceEnabled;
        }

        public boolean isEphemeral() {
            return ephemeral;
        }

        public void setEphemeral(boolean ephemeral) {
            this.ephemeral = ephemeral;
        }

        public boolean isFailFast() {
            return failFast;
        }

        public void setFailFast(boolean failFast) {
            this.failFast = failFast;
        }
    }

    public static class Transport {
        /**
         * 在dashboard显示的应用名，无需配置，会自动被 {@link ServerProperties#appName} 覆盖
         */
        private String appName;
        /**
         * 当前实例的IP地址，无需配置，会自动被 {@link ServerProperties#ip} 覆盖
         * 用以和 sentinel-dashboard 进行通讯使用，比如发送心跳、拉取实时监控等
         */
        private String clientIp;
        /**
         * 和 sentinel-dashboard 通信的端口号，sentinel-dashboard会定时请求拉取实时监控数据
         */
        private String port = "8719";
        /**
         * Sentinel dashboard address, won't try to connect dashboard when address is
         * empty {@link TransportConfig#CONSOLE_SERVER}.
         */
        private String dashboard = "";
        /**
         * Send heartbeat interval millisecond
         * {@link TransportConfig#HEARTBEAT_INTERVAL_MS}.
         */
        private String heartbeatIntervalMs;

        private int idleSeconds;

        public String getHeartbeatIntervalMs() {
            return heartbeatIntervalMs;
        }

        public void setHeartbeatIntervalMs(String heartbeatIntervalMs) {
            this.heartbeatIntervalMs = heartbeatIntervalMs;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getDashboard() {
            return dashboard;
        }

        public void setDashboard(String dashboard) {
            this.dashboard = dashboard;
        }

        public String getClientIp() {
            return clientIp;
        }

        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }

        public int getIdleSeconds() {
            return idleSeconds;
        }

        public void setIdleSeconds(int idleSeconds) {
            this.idleSeconds = idleSeconds;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }
    }
}
