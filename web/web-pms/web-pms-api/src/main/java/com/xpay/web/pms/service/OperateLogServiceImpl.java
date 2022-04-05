package com.xpay.web.pms.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.enums.user.OperateLogTypeEnum;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.user.dto.PmsOperateLogDto;
import com.xpay.facade.user.service.PmsOperateLogFacade;
import com.xpay.web.api.common.ddo.vo.OperateLogVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.OperateLogService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OperateLogServiceImpl implements OperateLogService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    PmsOperateLogFacade pmsOperateLogFacade;

    public PageResult<List<OperateLogVo>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo) {
        PageResult<List<PmsOperateLogDto>> pageResult = pmsOperateLogFacade.listOperateLogPage(paramMap, pageQuery);
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
    public void logLogout(String content, UserModel userModel) {
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

    private void appendLog(String content, UserModel userModel, OperateLogTypeEnum operateType) {
        PmsOperateLogDto operateLog = new PmsOperateLogDto();
        try{
            operateLog.setContent(content);
            operateLog.setCreateTime(new Date());
            operateLog.setOperateType(operateType.getValue());
            operateLog.setLoginName(userModel.getLoginName());
            pmsOperateLogFacade.createOperateLog(operateLog);
        }catch(Exception e){
            logger.error("添加操作日志时发生异常 PmsOperateLog = {}", JsonUtil.toJson(operateLog), e);
        }
    }
}
