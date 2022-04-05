package com.xpay.starter.sharding.config;

import com.xpay.starter.sharding.properties.ShardingProperties;
import com.xpay.starter.sharding.utils.ShardingRuleUtil;
import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.apache.shardingsphere.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(SpringBootShardingRuleConfigurationProperties.class)
@AutoConfigureBefore(SpringBootConfiguration.class)
@EnableConfigurationProperties(ShardingProperties.class)
@Configuration
public class ShardingAutoConfiguration {

    public ShardingAutoConfiguration(ShardingProperties properties){
        ShardingRuleUtil.initShardingRule(properties.getRules());
    }
}
