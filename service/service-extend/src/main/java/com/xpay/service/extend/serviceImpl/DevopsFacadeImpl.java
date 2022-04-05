package com.xpay.service.extend.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.extend.dto.PublishRecordDto;
import com.xpay.facade.extend.enums.BuildResultEnum;
import com.xpay.facade.extend.service.DevopsFacade;
import com.xpay.facade.extend.vo.IdcVo;
import com.xpay.facade.extend.vo.PublishInfoVo;
import com.xpay.service.extend.biz.devops.DevopsBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class DevopsFacadeImpl implements DevopsFacade {
    @Autowired
    DevopsBiz devopsBiz;

    @Override
    public boolean addPublish(PublishRecordDto publishRecord, Integer buildType) {
        return devopsBiz.addPublish(publishRecord, buildType);
    }

    @Override
    public boolean republish(long id, String relayApp, String remark, String notifyUrl, String modifier) {
        return devopsBiz.republish(id, relayApp, remark, notifyUrl, modifier);
    }

    @Override
    public void publishResultCallback(Long id, BuildResultEnum result){
        devopsBiz.publishResultCallback(id, result);
    }

    @Override
    public boolean auditPublishRecord(long id, Integer status, String remark, String modifier){
        return devopsBiz.auditPublishRecord(id, status, remark, modifier);
    }

    @Override
    public boolean cancelPublish(Long id, String modifier){
        return devopsBiz.cancelPublish(id, modifier);
    }

    @Override
    public PublishRecordDto getById(long id) {
        return devopsBiz.getDtoById(id);
    }

    @Override
    public PageResult<List<PublishRecordDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery){
        return devopsBiz.listPage(paramMap, pageQuery);
    }

    @Override
    public PublishInfoVo getPublishInfo(){
        return devopsBiz.getPublishInfo();
    }

    @Override
    public IdcVo getCurrIdcFlow(){
        return devopsBiz.getCurrIdcFlow();
    }

    @Override
    public boolean flowSwitch(List<String> toIdcList, boolean checkPublishing, String operator){
        return devopsBiz.flowSwitch(toIdcList, checkPublishing, operator);
    }

    @Override
    public boolean isInSameIdc(String idcCode, String address){
        return devopsBiz.isInSameIdc(idcCode, address);
    }

    @Override
    public boolean syncIdcPublish(String toIdc, List<Long> publishIdList, String syncMsg, String notifyUrl, String operator){
        return devopsBiz.syncIdcPublish(toIdc, publishIdList, syncMsg, notifyUrl, operator);
    }
}
