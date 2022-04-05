package com.xpay.web.portal.web.controller;

import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 基础控制器类
 */
public abstract class BaseController {
    @Autowired(required = false)
    protected OperateLogService operateLogService;

    public HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 添加数据时记录日志.
     *
     * @param content .
     * @param userModel 操作者
     */
    public void logAdd(String content, UserModel userModel){
        if(operateLogService != null){
            operateLogService.logAdd(content, userModel);
        }
    }

    /**
     * 更新数据时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logEdit(String content, UserModel userModel){
        if(operateLogService != null){
            operateLogService.logEdit(content, userModel);
        }
    }

    /**
     * 删除数据时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logDelete(String content, UserModel userModel){
        if(operateLogService != null){
            operateLogService.logEdit(content, userModel);
        }
    }

    /**
     * 查询数据时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logQuery(String content, UserModel userModel){
        if(operateLogService != null){
            operateLogService.logEdit(content, userModel);
        }
    }
}
