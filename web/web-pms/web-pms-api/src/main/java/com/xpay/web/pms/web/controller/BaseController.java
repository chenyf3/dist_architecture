package com.xpay.web.pms.web.controller;

import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Author: Cmf
 * Date: 2019/10/11
 * Time: 9:51
 * Description:
 */
public abstract class BaseController {
    @Autowired(required = false)
    private OperateLogService operateLogService;

    public HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 登录系统成功时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logLoginSuccess(String content, UserModel userModel){
        if(operateLogService != null){
            operateLogService.logLoginSuccess(content, userModel);
        }
    }

    /**
     * 登录系统失败时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logLoginError(String content, UserModel userModel){
        if(operateLogService != null){
            operateLogService.logLoginError(content, userModel);
        }
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
