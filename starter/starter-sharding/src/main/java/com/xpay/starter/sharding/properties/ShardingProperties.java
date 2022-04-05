package com.xpay.starter.sharding.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.sharding")
public class ShardingProperties {
    /**
     * 分片规则，其中 key为逻辑表名(如：tbl_order)，value为该表的分组分片规则
     */
    private Map<String, List<GroupRule>> rules = new LinkedHashMap<>();

    public Map<String, List<GroupRule>> getRules() {
        return rules;
    }

    public void setRules(Map<String, List<GroupRule>> rules) {
        this.rules = rules;
    }
}
