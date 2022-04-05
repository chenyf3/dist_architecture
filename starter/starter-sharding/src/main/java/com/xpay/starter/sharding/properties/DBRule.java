package com.xpay.starter.sharding.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * 分库规则
 */
public class DBRule {
    public final static String HASH_SEPARATOR = ",";

    /**
     * 当前数据源名称（需要和 ShardingSphere 中配置的数据源名称一致）
     */
    private String name;
    /**
     * 当前数据源负责的hash值列表，有多个值时用英文的逗号","分隔，有几个hash值就需要在这个库中建几个表
     */
    private String hashValue = "";
    /**
     * 当前数据源下的分表模式，常用在分库id和分表id不是同一个字段的情况
     */
    private Mode mode = Mode.RANGE;
    /**
     * 当前数据源下的表，可以直接配置，也可以根据hashValues个数以及GroupRule中的startId、endId自动计算 {@link #calcTableRules(String, Long, Long, Integer)}
     */
    private List<TableRule> tableRules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public List<TableRule> getTableRules() {
        return tableRules;
    }

    public void setTableRules(List<TableRule> tableRules) {
        this.tableRules = tableRules;
    }

    public void calcTableRules(String logicTable, Long groupStartId, Long groupEndId, Integer tableRows){
        if(this.tableRules == null){
            this.tableRules = new ArrayList<>();
        }

        int totalTableCount = hashValue.split(HASH_SEPARATOR).length;
        if(groupEndId != -1){
            double rows = Math.ceil((groupEndId - groupStartId) / totalTableCount);
            tableRows = (int) rows;
        }

        Long currStartId = groupStartId;
        Long currEndId = 0L;
        for (int i=0; i<totalTableCount; i++) {
            String tableName = logicTable + "_" + (i+1);//表名规则：逻辑表名_序号，如：t_order_1

            currEndId = currStartId + tableRows;
            if(i == totalTableCount-1){
                currEndId = groupEndId;
            }else if(groupEndId != -1 && currEndId > groupEndId){
                throw new RuntimeException(tableName + " 当前表的结束id("+currEndId+")已超过分组的结束id("+groupEndId+")，请设置合理的id区间");
            }

            TableRule tableRule = new TableRule();
            tableRule.setStartId(currStartId);
            tableRule.setEndId(currEndId);
            tableRule.setName(tableName);

            this.tableRules.add(tableRule);
            currStartId = tableRule.getEndId() + 1;
        }
    }

    public enum Mode {
        //表示根据id范围区间分表
        RANGE,
        //表示根据id取模方式，即：id % 分表数
        MOD
    }
}
