package com.xpay.starter.plugin.util;

import com.xpay.starter.plugin.properties.RedisProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import java.util.*;

/**
 * 生成 RedissonConnectionFactory、RedissonClient 的工具类
 * @author chenyf
 */
public class RedissonConnectionUtil {

    public static RedissonConnectionFactory createRedissonConnectionFactory(RedisProperties redisProperties){
        RedissonClient redisson = createRedissonClient(redisProperties);
        return new RedissonConnectionFactory(redisson);
    }

    public static RedissonClient createRedissonClient(RedisProperties redisProperties){
        Integer connectTimeout = (int) redisProperties.getTimeout().toMillis();
        Integer timeout = (int) redisProperties.getTimeout().toMillis();
        RedisProperties.Pool pool = redisProperties.getPool();

        Config config = new Config();
        if (redisProperties.getSentinel() != null) {
            List<String> nodeList = redisProperties.getSentinel().getNodes();
            String[] nodes = convert(nodeList, redisProperties.isSsl());
            config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setSentinelPassword(redisProperties.getSentinel().getPassword())//sentinel的访问密码
                    .setPassword(redisProperties.getPassword())//master的访问密码
                    .setDatabase(redisProperties.getDatabase())
                    .setClientName(redisProperties.getClientName())
                    .setReadMode(ReadMode.MASTER)
                    .setSubscriptionMode(SubscriptionMode.MASTER)//只在master节点订阅，因为分布式锁对数据一致性要求较高
                    .setConnectTimeout(connectTimeout)
                    .setTimeout(timeout)
                    .setMasterConnectionPoolSize(pool.getMaxActive())
                    .setIdleConnectionTimeout((int)pool.getIdleConnectionTimeout().toMillis())
                    .setMasterConnectionMinimumIdleSize(pool.getMinIdle());
        } else if (redisProperties.getCluster() != null) {
            List<String> nodeList = redisProperties.getCluster().getNodes();
            String[] nodes = convert(nodeList, redisProperties.isSsl());
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setPassword(redisProperties.getPassword())//集群访问密码
                    .setClientName(redisProperties.getClientName())
                    .setReadMode(ReadMode.MASTER)//只在master节点读取
                    .setSubscriptionMode(SubscriptionMode.MASTER)//只在master节点订阅，因为分布式锁对数据一致性要求较高
                    .setConnectTimeout(connectTimeout)
                    .setTimeout(timeout)
                    .setMasterConnectionPoolSize(pool.getMaxActive())
                    .setIdleConnectionTimeout((int)pool.getIdleConnectionTimeout().toMillis())
                    .setMasterConnectionMinimumIdleSize(pool.getMinIdle());
        } else {
            List<String> nodeList = Collections.singletonList(redisProperties.getHost() + ":" + redisProperties.getPort());
            String[] nodes = convert(nodeList, redisProperties.isSsl());
            config.useSingleServer()
                    .setAddress(nodes[0])
                    .setConnectTimeout(timeout)
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword())
                    .setClientName(redisProperties.getClientName())
                    .setConnectTimeout(connectTimeout)
                    .setTimeout(timeout)
                    .setConnectionPoolSize(pool.getMaxActive())
                    .setIdleConnectionTimeout((int)pool.getIdleConnectionTimeout().toMillis())
                    .setConnectionMinimumIdleSize(pool.getMinIdle());
        }

        return Redisson.create(config);
    }

    private static String[] convert(List<String> nodeAddressList, boolean isSSL) {
        List<String> nodes = new ArrayList<>(nodeAddressList.size());
        for (String node : nodeAddressList) {
            if (node.startsWith("redis://") || node.startsWith("rediss://")) {
                nodes.add(node);
            } else if (isSSL) {
                nodes.add("rediss://" + node);
            } else {
                nodes.add("redis://" + node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }
}
