package com.xpay.facade.message.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.message.dto.AmqTraceDto;

import java.util.List;
import java.util.Map;

public interface AmqTraceFacade {

    public boolean resendOriMsg(Long recordId) throws BizException;

    /**
     * 发送补偿消息
     * @param msgJsonStr
     * @param operator
     * @return
     */
    public boolean sendCompensate(String msgJsonStr, String operator) throws BizException ;

    public PageResult<List<AmqTraceDto>> listAmqTracePage(Map<String, Object> paramMap, PageQuery pageQuery);

    public AmqTraceDto getAmqTraceById(Long id);

    public List<AmqTraceDto> listAmqTraceByTraceId(Long traceId);

    public List<AmqTraceDto> listAmqTraceByTrxNo(Long trxNo);


}
