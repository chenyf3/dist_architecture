/*
 * Powered By [xpay.com]
 */
package com.xpay.service.message.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.facade.message.dto.AmqTraceDto;
import com.xpay.service.message.entity.AmqTrace;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AmqTraceDao extends MyBatisDao<AmqTrace, Long> {

    public AmqTraceDto getOriMsgByTraceId(String traceId){
        return getOne("getOriMsgByTraceId", traceId);
    }

    public AmqTrace getFirstConsumeRecordByTraceIdAndClientFlag(String traceId, String clientFlag){
        Map<String, Object> param = new HashMap<>();
        param.put("traceId", traceId);
        param.put("clientFlag", clientFlag);
        return getOne("getFirstConsumeRecordByTraceIdAndClientFlag", param);
    }

    public AmqTrace getLastConsumeRecordByTraceIdAndClientFlag(String traceId, String clientFlag){
        Map<String, Object> param = new HashMap<>();
        param.put("traceId", traceId);
        param.put("clientFlag", clientFlag);
        return getOne("getLastConsumeRecordByTraceIdAndClientFlag", param);
    }
}
