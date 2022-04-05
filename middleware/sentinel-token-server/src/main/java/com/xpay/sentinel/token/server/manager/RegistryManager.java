package com.xpay.sentinel.token.server.manager;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.xpay.sentinel.token.server.config.NacosConst;
import com.xpay.sentinel.token.server.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务注册管理器
 * @author chenyf
 */
public class RegistryManager {
    private static final Logger log = LoggerFactory.getLogger(RegistryManager.class);
    private final AtomicBoolean initFinish = new AtomicBoolean(false);
    private final ServerProperties.Registry registry;
    private NamingService namingService;

    public RegistryManager(ServerProperties.Registry registry) {
        this.registry = registry;
    }

    public void init(){
        if(initFinish.compareAndSet(false, true)){
            if(registry.getServerAddr() != null && registry.getServerAddr().trim().length() > 0){
                buildNamingService();
                doServerRegistry();
            }
        }
    }

    public void destroy() throws Exception {
        if(namingService == null) {
            return;
        }

        String serviceName = registry.getAppName();
        String group = registry.getGroupId();
        String ip = registry.getIp();
        Integer port = registry.getPort();
        String cluster = registry.getClusterName();

        namingService.deregisterInstance(serviceName, group, ip, port, cluster);
        this.namingService = null;
    }

    private void doServerRegistry(){
        String serviceName = registry.getAppName();
        String group = registry.getGroupId();
        Instance instance = new Instance();
        instance.setIp(registry.getIp());
        instance.setPort(registry.getPort());
        instance.setWeight(registry.getWeight());
        instance.setClusterName(registry.getClusterName());
        instance.setEnabled(registry.isInstanceEnabled());
        instance.setEphemeral(registry.isEphemeral());
        instance.setMetadata(registry.getMetadata());

        try {
            namingService.registerInstance(serviceName, group, instance);
        } catch (Exception e) {
            if (registry.isFailFast()) {
                log.error("nacos registry, {} register failed...{},", serviceName, registry.toString(), e);
                ReflectionUtils.rethrowRuntimeException(e);
            } else {
                log.warn("Failfast is false. {} register failed...{},", serviceName, registry.toString(), e);
            }
        }
    }

    private NamingService buildNamingService() {
        Properties properties = new Properties();
        properties.put(NacosConst.SERVER_ADDR, registry.getServerAddr());
        properties.put(NacosConst.NAMESPACE, registry.getNamespace());
        properties.put(NacosConst.USERNAME, registry.getUsername());
        properties.put(NacosConst.PASSWORD, registry.getPassword());

        if (Objects.isNull(namingService)) {
            synchronized (RegistryManager.class) {
                if (Objects.isNull(namingService)) {
                    try {
                        namingService = NacosFactory.createNamingService(properties);
                    } catch (NacosException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return namingService;
    }
}
