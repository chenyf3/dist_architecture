package com.xpay.sentinel.token.server.manager;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xpay.sentinel.token.server.config.NacosConst;
import com.xpay.sentinel.token.server.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TokenServer管理配置器，负责服务端启动，流控规则动态更新等
 * @author chenyf
 */
public class TokenServerManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean initFinish = new AtomicBoolean(false);
    private final ServerProperties properties;
    private ClusterTokenServer tokenServer;

    public TokenServerManager(ServerProperties properties) {
        this.properties = properties;
    }

    public void init(){
        if(initFinish.compareAndSet(false, true)){
            initSystemProperty();
            doServerConfig();
        }
    }

    public void destroy() throws Exception {
        if(tokenServer != null){
            tokenServer.stop();
        }
    }

    private void initSystemProperty() {
        ServerProperties.Transport transport = properties.getTransport();
        if (StringUtils.isEmpty(System.getProperty("csp.sentinel.app.name"))
                && StringUtils.hasText(transport.getAppName())) {
            System.setProperty("csp.sentinel.app.name", transport.getAppName());
        }
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))
                && StringUtils.hasText(transport.getPort())) {
            System.setProperty(TransportConfig.SERVER_PORT, transport.getPort());
        }
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))
                && StringUtils.hasText(transport.getDashboard())) {
            System.setProperty(TransportConfig.CONSOLE_SERVER, transport.getDashboard());
        }
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_INTERVAL_MS))
                && StringUtils.hasText(transport.getHeartbeatIntervalMs())) {
            System.setProperty(TransportConfig.HEARTBEAT_INTERVAL_MS, transport.getHeartbeatIntervalMs());
        }
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_CLIENT_IP))
                && StringUtils.hasText(transport.getClientIp())) {
            System.setProperty(TransportConfig.HEARTBEAT_CLIENT_IP, transport.getClientIp());
        }
    }

    private void doServerConfig() {
        ServerProperties.RuleServer ruleServer = this.properties.getRules();
        ServerProperties.Transport transport = this.properties.getTransport();
        Integer serverPort = this.properties.getPort();

        Properties properties = new Properties();
        properties.put(NacosConst.SERVER_ADDR, ruleServer.getServerAddr());
        properties.put(NacosConst.NAMESPACE, ruleServer.getNamespace());
        properties.put(NacosConst.USERNAME, ruleServer.getUsername());
        properties.put(NacosConst.PASSWORD, ruleServer.getPassword());

        //设置 flow rule 规则的来源，实际上就是每个应用的flowRule在配置中心的配置名称
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            String dataId = namespace + NacosConst.FLOW_DATA_ID_POSTFIX;
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(properties, ruleServer.getGroupId(),
                    dataId, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            logger.info("FlowRule规则已添加 groupId={} dataId={}", ruleServer.getGroupId(), dataId);
            return ds.getProperty();
        });
        // 设置 param flow rule 规则的来源，实际上就是每个应用的ParamFlowRule在配置中心的配置名称
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            String dataId = namespace + NacosConst.PARAM_FLOW_DATA_ID_POSTFIX;
            ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(properties, ruleServer.getGroupId(),
                    dataId, source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
            logger.info("ParamFlowRule规则已添加 groupId={} dataId={}", ruleServer.getGroupId(), dataId);
            return ds.getProperty();
        });

        // 配置当前 TokenServer 支持哪些应用，一个 namespace 就代表一个应用
        ReadableDataSource<String, Set<String>> namespaceDs = new NacosDataSource<>(properties, ruleServer.getGroupId(),
                ruleServer.getNamespaceSetDataId(), source -> JSON.parseObject(source, new TypeReference<Set<String>>() {}));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());

        //配置当前 TokenServer 为TokenClient服务的端口号、连接空闲时间
        ServerTransportConfig transportConfig = new ServerTransportConfig();
        transportConfig.setPort(serverPort);
        transportConfig.setIdleSeconds(transport.getIdleSeconds());
        ClusterServerConfigManager.loadGlobalTransportConfig(transportConfig);

        //启动一个 ClusterTokenServer 实例
        tokenServer = new SentinelDefaultTokenServer();
        try {
            tokenServer.start();
        } catch (Exception e){
            throw new RuntimeException("SentinelDefaultTokenServer启动失败", e);
        }
    }
}
