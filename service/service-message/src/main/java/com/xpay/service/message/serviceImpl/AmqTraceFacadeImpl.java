package com.xpay.service.message.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.message.dto.AmqTraceDto;
import com.xpay.facade.message.service.AmqTraceFacade;
import com.xpay.service.message.biz.mq.AmqTraceBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class AmqTraceFacadeImpl implements AmqTraceFacade {
    @Autowired
    AmqTraceBiz amqTraceBiz;

    @Override
    public boolean resendOriMsg(Long recordId) {
        return amqTraceBiz.resendOriMsg(recordId);
    }

    public boolean sendCompensate(String msgJsonStr, String operator){
        return amqTraceBiz.sendCompensate(msgJsonStr, operator);
    }

    @Override
    public PageResult<List<AmqTraceDto>> listAmqTracePage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return amqTraceBiz.listPage(paramMap, pageQuery);
    }

    @Override
    public AmqTraceDto getAmqTraceById(Long id) {
        return amqTraceBiz.getAmqTraceById(id);
    }

    @Override
    public List<AmqTraceDto> listAmqTraceByTraceId(Long traceId) {
        return amqTraceBiz.listAmqTraceByTraceId(traceId);
    }

    @Override
    public List<AmqTraceDto> listAmqTraceByTrxNo(Long trxNo) {
        return amqTraceBiz.listAmqTraceByTrxNo(trxNo);
    }
}
