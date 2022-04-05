package com.xpay.starter.plugin.util;

import com.xpay.starter.plugin.properties.RedisProperties;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成 JedisConnectionFactory 的工具类
 * @author chenyf
 */
public class JedisConnectionUtil {

    public static JedisConnectionFactory createJedisConnectionFactory(RedisProperties properties) {
        JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(properties);
        if (properties.getSentinel() != null) {
            return new JedisConnectionFactory(getSentinelConfig(properties), clientConfiguration);
        }
        if (properties.getCluster() != null) {
            return new JedisConnectionFactory(getClusterConfiguration(properties), clientConfiguration);
        }
        return new JedisConnectionFactory(getStandaloneConfig(properties), clientConfiguration);
    }

    private static RedisSentinelConfiguration getSentinelConfig(RedisProperties properties) {
        RedisProperties.Sentinel sentinelProperties = properties.getSentinel();
        if (sentinelProperties != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(sentinelProperties.getMaster());
            config.setSentinels(createSentinels(sentinelProperties));
            if (properties.getPassword() != null) {
                config.setPassword(RedisPassword.of(properties.getPassword()));
            }
            if (sentinelProperties.getPassword() != null) {
                config.setSentinelPassword(RedisPassword.of(sentinelProperties.getPassword()));
            }
            config.setDatabase(properties.getDatabase());
            return config;
        }
        return null;
    }

    private static RedisClusterConfiguration getClusterConfiguration(RedisProperties properties) {
        if (properties.getCluster() == null) {
            return null;
        }
        RedisProperties.Cluster clusterProperties = properties.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        if (properties.getPassword() != null) {
            config.setPassword(RedisPassword.of(properties.getPassword()));
        }
        return config;
    }

    private static RedisStandaloneConfiguration getStandaloneConfig(RedisProperties properties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getHost());
        config.setPort(properties.getPort());
        config.setPassword(RedisPassword.of(properties.getPassword()));
        config.setDatabase(properties.getDatabase());
        return config;
    }

    private static JedisClientConfiguration getJedisClientConfiguration(RedisProperties properties) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(properties, JedisClientConfiguration.builder());
        RedisProperties.Pool pool = properties.getJedis().getPool();
        if (pool != null) {
            applyPooling(pool, builder);
        }
        return builder.build();
    }

    private static JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(
            RedisProperties properties, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        if (properties.isSsl()) {
            builder.useSsl();
        }
        if (properties.getTimeout() != null) {
            Duration timeout = properties.getTimeout();
            builder.readTimeout(timeout).connectTimeout(timeout);
        }
        if (StringUtils.hasText(properties.getClientName())) {
            builder.clientName(properties.getClientName());
        }
        return builder;
    }

    private static void applyPooling(RedisProperties.Pool pool,
                              JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(jedisPoolConfig(pool));
    }

    private static JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getTimeBetweenEvictionRuns() != null) {
            config.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns().toMillis());
        }
        if (pool.getMaxWait() != null) {
            config.setMaxWaitMillis(pool.getMaxWait().toMillis());
        }
        return config;
    }

    private static List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : sentinel.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            }
            catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", ex);
            }
        }
        return nodes;
    }
}
