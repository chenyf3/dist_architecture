package com.xpay.starter.sharding.algorithm;

import com.xpay.starter.sharding.utils.ShardingRuleUtil;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 数据库分片逻辑
 * @author chenyf
 */
public class DatabasePreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        String logicTable = preciseShardingValue.getLogicTableName();
        Long currValue = preciseShardingValue.getValue();
        return ShardingRuleUtil.matchDBRule(logicTable, currValue).getName();
    }
}
