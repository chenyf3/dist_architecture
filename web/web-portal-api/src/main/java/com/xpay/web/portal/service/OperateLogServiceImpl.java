package com.xpay.web.portal.service;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.user.OperateLogTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.user.dto.PortalOperateLogDto;
import com.xpay.facade.user.service.PortalOperateLogFacade;
import com.xpay.web.api.common.ddo.vo.OperateLogVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.OperateLogService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OperateLogServiceImpl implements OperateLogService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    PortalOperateLogFacade portalOperateLogFacade;

    @Override
    public PageResult<List<OperateLogVo>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo){
        if(StringUtil.isEmpty(mchNo)){
            throw new BizException(BizException.BIZ_INVALID, "商户编号为空！");
        }else if(paramMap == null){
            paramMap = new HashMap<>();
        }
        paramMap.put("mchNo", mchNo);

        PageResult<List<PortalOperateLogDto>> pageResult = portalOperateLogFacade.listOperateLogPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(pageResult.getData(), OperateLogVo.class), pageResult);
    }

    @Override
    public void logLoginSuccess(String content, UserModel userModel) {
        appendLog(content, userModel, OperateLogTypeEnum.LOGIN);
    }

    @Override
    public void logLoginError(String content, UserModel userModel) {
        appendLog(content, userModel, OperateLogTypeEnum.LOGIN);
    }

    @Override
    public void logLogout(String content, UserModel userModel){
        appendLog(content, userModel, OperateLogTypeEnum.LOGOUT);
    }

    @Override
    public void logAdd(String content, UserModel userModel) {
        appendLog(content, userModel, OperateLogTypeEnum.CREATE);
    }

    @Override
    public void logEdit(String content, UserModel userModel) {
        appendLog(content, userModel, OperateLogTypeEnum.MODIFY);
    }

    @Override
    public void logDelete(String content, UserModel userModel) {
        appendLog(content, userModel, OperateLogTypeEnum.DELETE);
    }

    @Override
    public void logQuery(String content, UserModel userModel) {
        appendLog(content, userModel, OperateLogTypeEnum.QUERY);
    }

    private void appendLog(String content, UserModel userModel, OperateLogTypeEnum operateType){
        PortalOperateLogDto operateLog = new PortalOperateLogDto();
        try{
            operateLog.setContent(content);
            operateLog.setCreateTime(new Date());
            operateLog.setOperateType(operateType.getValue());
            operateLog.setLoginName(userModel.getLoginName());
            operateLog.setIp(userModel.getRequestIp() != null ? userModel.getRequestIp() : "");
            operateLog.setStatus(PublicStatus.ACTIVE);
            operateLog.setMchNo(userModel.getMchNo());
            portalOperateLogFacade.createOperateLog(operateLog);
        }catch(Throwable e){
            logger.error("添加操作日志时发生异常 PortalOperateLog = {}", JsonUtil.toJson(operateLog), e);
        }
    }
}
