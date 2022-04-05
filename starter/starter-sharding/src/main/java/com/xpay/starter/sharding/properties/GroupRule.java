package com.xpay.starter.sharding.properties;

import java.util.List;

/**
 * 分组规则
 */
public class GroupRule {
    /**
     * 分组名称
     */
    private String name;
    /**
     * 当前分组的起始id
     */
    private Long startId;
    /**
     * 当前分组的结束id，等于-1时表示无限制，假设该分组下某个库中有N张分表，则第N张表的写入将不会有id范围限制，
     * 而第1张表 至 第N-1张表的数据量由 {@link #perTableRow} 属性指定
     */
    private Long endId = -1L;
    /**
     * 预估每张表的记录数（默认1000万行），仅当 endId = -1 时有效，当 endId != -1 时，此字段值 = (endId - startId) / 库的表数 来计算得到
     */
    private Integer perTableRow = 10000000;
    /**
     * 当前分组下的数据源列表
     */
    private List<DBRule> dbRules;

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

    public Integer getPerTableRow() {
        return perTableRow;
    }

    public void setPerTableRow(Integer perTableRow) {
        this.perTableRow = perTableRow;
    }

    public List<DBRule> getDbRules() {
        return dbRules;
    }

    public void setDbRules(List<DBRule> dbRules) {
        this.dbRules = dbRules;
    }
}
