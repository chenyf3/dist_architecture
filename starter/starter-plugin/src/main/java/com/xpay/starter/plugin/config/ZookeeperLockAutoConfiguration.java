package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.annotation.ConditionalOnPropertyNotEmpty;
import com.xpay.starter.plugin.client.ZKClient;
import com.xpay.starter.plugin.pluginImpl.ZkLock;
import com.xpay.starter.plugin.plugins.DistributedLock;
import com.xpay.starter.plugin.properties.ZookeeperProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "zookeeper.lockEnabled", havingValue = "true")
@ConditionalOnClass(CuratorFramework.class)
@EnableConfigurationProperties(ZookeeperProperties.class)
@Configuration
public class ZookeeperLockAutoConfiguration {

    @ConditionalOnPropertyNotEmpty(value = "zookeeper.urls")
    @Bean(name="zookeeperLock", destroyMethod = "destroy")
    public DistributedLock<InterProcessMutex> zookeeperLock(ZookeeperProperties zookeeperProperties) {
        ZKClient zkClient = new ZKClient(zookeeperProperties);
        return new ZkLock(zkClient);
    }
}
