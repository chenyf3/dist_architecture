package com.xpay.common.statics.result;

import com.xpay.common.statics.dto.es.EsAgg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES聚合统计结果
 * @author chenyf
 */
public class EsAggResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 分组字段
     */
    private String groupField;
    /**
     * 有分组时的统计结果，其中key为字段名，value为统计结果列表
     */
    Map<String, List<EsAgg>> aggResults = new HashMap<>();

    public String getGroupField() {
        return groupField;
    }

    public void setGroupField(String groupField) {
        this.groupField = groupField;
    }

    public Map<String, List<EsAgg>> getAggResults() {
        return aggResults;
    }

    public void setAggResults(Map<String, List<EsAgg>> aggResults) {
        this.aggResults = aggResults;
    }
}
