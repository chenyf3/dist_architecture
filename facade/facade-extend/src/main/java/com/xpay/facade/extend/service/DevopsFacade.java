package com.xpay.facade.extend.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.extend.dto.PublishRecordDto;
import com.xpay.facade.extend.enums.BuildResultEnum;
import com.xpay.facade.extend.vo.IdcVo;
import com.xpay.facade.extend.vo.PublishInfoVo;

import java.util.List;
import java.util.Map;

public interface DevopsFacade {

    public boolean addPublish(PublishRecordDto publishRecord, Integer buildType) throws BizException;

    public boolean republish(long id, String relayApp, String remark, String notifyUrl, String modifier) throws BizException;

    public void publishResultCallback(Long id, BuildResultEnum result) throws BizException;

    public boolean auditPublishRecord(long id, Integer status, String remark, String modifier) throws BizException;

    public boolean cancelPublish(Long id, String modifier) throws BizException;

    public PublishRecordDto getById(long id);

    public PageResult<List<PublishRecordDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery);

    public PublishInfoVo getPublishInfo();

    public IdcVo getCurrIdcFlow() throws BizException;

    public boolean flowSwitch(List<String> toIdcList, boolean checkPublishing, String operator) throws BizException;

    public boolean syncIdcPublish(String toIdc, List<Long> publishIdList, String syncMsg, String notifyUrl, String operator) throws BizException;

    public boolean isInSameIdc(String idcCode, String address);
}
