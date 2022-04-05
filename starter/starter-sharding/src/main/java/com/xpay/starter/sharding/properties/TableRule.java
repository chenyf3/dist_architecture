package com.xpay.starter.sharding.properties;

/**
 * 分表规则
 */
public class TableRule {
    /**
     * 真实表名（需要和数据库中的表名一致）
     */
    private String name;
    /**
     * 当前表的起始id
     */
    private Long startId;
    /**
     * 当前表的结束id，设置为-1表示无限制
     */
    private Long endId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStartId() {
        return startId;
    }

    public void setStartId(Long startId) {
        this.startId = startId;
    }

    public Long getEndId() {
        return endId;
    }

    public void setEndId(Long endId) {
        this.endId = endId;
    }
}
