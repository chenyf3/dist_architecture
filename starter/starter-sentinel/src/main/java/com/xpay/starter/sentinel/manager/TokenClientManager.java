package com.xpay.starter.sentinel.manager;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.xpay.starter.sentinel.config.NacosConst;
import com.xpay.starter.sentinel.config.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 集群流控模式下，TokenClient管理器，主要是负责从注册中心发现TokenServer的实例列表，然后从中选择一个来连接
 * @author chenyf
 */
public class TokenClientManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean initFinish = new AtomicBoolean(false);
    private final ClientProperties properties;
    private NamingService namingService;
    private EventListener listener;
    private String currentServer;

    public TokenClientManager(ClientProperties properties){
        this.properties = properties;
    }

    public void init(){
        if(initFinish.compareAndSet(false, true)){
            doClientConfig();
        }
    }

    public void destroy() throws Exception {
        if(namingService == null){
            return;
        }

        String serviceName = properties.getTokenServer().getServiceName();
        String group = properties.getTokenServer().getGroupId();
        List<String> clusters = Collections.singletonList(properties.getTokenServer().getClusterName());
        namingService.unsubscribe(serviceName, group, clusters, listener);
    }

    private void doClientConfig() {
        ClientProperties.TokenServer tokenServer = properties.getTokenServer();
        Integer requestTimeout = tokenServer.getRequestTimeout();
        String serverHost = null;
        Integer serverPort = null;
        if (tokenServer.getServerHost() != null && tokenServer.getServerPort() != null) {
            serverHost = tokenServer.getServerHost();
            serverPort = tokenServer.getServerPort();
            currentServer = getInstanceId(serverHost, serverPort);
        } else if (tokenServer.getServerAddr() != null
                && tokenServer.getServerAddr().trim().length() > 0) {

            this.setSystemProperty(properties);

            this.buildNamingService();

            String appName = properties.getAppName();
            String serviceName = tokenServer.getServiceName();
            String group = tokenServer.getGroupId();
            List<String> clusters = Collections.singletonList(tokenServer.getClusterName());
            List<Instance> instanceList = null;
            try {
                instanceList = namingService.selectInstances(serviceName, group, clusters, true, false);
            } catch (Exception e) {
                throw new RuntimeException("从注册中心获取TokenServer实例列表失败", e);
            }

            Instance instance = selectOneInstance(properties.getAppName(), instanceList);
            if (instance == null) {
                logger.warn("注册中心未发现可用的TokenServer，在有可用的TokenServer之前客户端将采用本地计算 appName={}", appName);
            } else {
                serverHost = instance.getIp();
                serverPort = instance.getPort();
                currentServer = getInstanceId(serverHost, serverPort);
                logger.info("TokenServer选中 appName={} currentServer={}", appName, currentServer);
            }

            listener = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (event instanceof NamingEvent) {
                        List<Instance> instanceList = ((NamingEvent) event).getInstances();
                        if (!isNeedReSelectInstance(currentServer, instanceList)) {
                            return;
                        }

                        Instance instance = selectOneInstance(properties.getAppName(), instanceList);
                        String newServer = getInstanceId(instance.getIp(), instance.getPort());
                        List<String> instanceIdList = new ArrayList<>();
                        instanceList.forEach(e -> instanceIdList.add(getInstanceId(e.getIp(), e.getPort())));
                        if (newServer.equals(currentServer)) {
                            logger.info("接收到TokenServer实例列表变动事件，但本客户端选择的实例无变化，无需更新配置 appName={} " +
                                    "currentServer={} instanceList={}", appName, currentServer, JSON.toJSONString(instanceIdList));
                        } else {
                            logger.info("TokenServer变动 appName={} oldServer={} newServer={} instanceList={}",
                                    appName, currentServer, newServer, JSON.toJSONString(instanceIdList));
                            currentServer = newServer;
                            Integer requestTimeout = tokenServer.getRequestTimeout();
                            String serverHost = instance.getIp();
                            Integer serverPort = instance.getPort();
                            setupClusterClient(requestTimeout, serverHost, serverPort);
                        }
                    }
                }

                private boolean isNeedReSelectInstance(String currentServer, List<Instance> instanceList) {
                    if (instanceList == null || instanceList.isEmpty()) {
                        logger.info("接收到TokenServer实例变动事件，但Server实例列表为空 appName={} currentServer={}", appName, currentServer);
                        return false;
                    } else if(currentServer == null) {
                        return true;
                    }

//                    //判断当前连接的节点是不是还在实例列表中，如果已经不在，说明当前连接的节点已经挂了，需要重新选一个
//                    Instance instance = instanceList.stream()
//                            .filter(e -> getInstanceId(e.getIp(), e.getPort()).equals(currentServer))
//                            .findFirst()
//                            .orElse(null);
//                    if(instance == null){
//                        return true;
//                    }
                    return true;
                }
            };

            try {
                namingService.subscribe(serviceName, group, clusters, listener);
            } catch (Exception e) {
                throw new RuntimeException("从注册中心订阅失败", e);
            }
        } else {
            throw new RuntimeException("无法判断TokenServer的获取方式，请配置TokenServer地址或者注册中心信息！");
        }

        if(currentServer != null){
            setupClusterClient(requestTimeout, serverHost, serverPort);
        }
    }

    private void setupClusterClient(Integer requestTimeout, String serverHost, Integer serverPort){
        //设置TokenServer请求超时时间
        ClusterClientConfig clientConfig = new ClusterClientConfig();
        clientConfig.setRequestTimeout(requestTimeout);
        ClusterClientConfigManager.applyNewConfig(clientConfig);

        //设置TokenServer的地址、端口
        ClusterClientAssignConfig clientAssignConfig = new ClusterClientAssignConfig();
        clientAssignConfig.setServerHost(serverHost);
        clientAssignConfig.setServerPort(serverPort);
        ClusterClientConfigManager.applyNewAssignConfig(clientAssignConfig);

        //设置为client模式
        ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);
    }

    /**
     * 从节点列表中选择一个来进行连接，通过 排序 + hash值 的方式，能够确保同一个应用的不同实例都会选中同一个节点
     * @param appName
     * @param instanceList
     * @return
     */
    private Instance selectOneInstance(String appName, List<Instance> instanceList) {
        if(instanceList == null || instanceList.isEmpty()) {
            return null;
        }

        //按字典序排序
        Map<String, Instance> map = new TreeMap();
        instanceList.forEach(e -> map.put(getInstanceId(e.getIp(), e.getPort()), e));
        instanceList.clear();
        instanceList.addAll(map.values());

        int hash = appName.hashCode();
        hash = hash ^ (hash >>> 16);
        hash = hash & Integer.MAX_VALUE;//把hash值限定在 0 ~ Integer.MAX_VALUE 之间
        int index = hash % instanceList.size();
        return instanceList.get(index);
    }

    private void buildNamingService(){
        ClientProperties.TokenServer tokenServer = properties.getTokenServer();
        Properties properties = new Properties();
        properties.put(NacosConst.SERVER_ADDR, tokenServer.getServerAddr());
        properties.put(NacosConst.NAMESPACE, tokenServer.getNamespace());
        properties.put(NacosConst.USERNAME, tokenServer.getUsername());
        properties.put(NacosConst.PASSWORD, tokenServer.getPassword());

        try {
            namingService = NamingFactory.createNamingService(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getInstanceId(String ip, Integer port){
        return ip + ":" + port;
    }

    private void setSystemProperty(ClientProperties properties){
        System.setProperty("project.name", properties.getAppName());//为了在订阅者列表处能看到订阅者的应用名
    }
}
