package com.xpay.web.api.common.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.web.api.common.ddo.vo.OperateLogVo;
import com.xpay.web.api.common.model.UserModel;

import java.util.List;
import java.util.Map;

public interface OperateLogService {
    /**
     * 分页查询操作日志
     * @param paramMap
     * @param pageQuery
     * @param mchNo
     * @return
     */
    public PageResult<List<OperateLogVo>> listOperateLogPage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo);

    /**
     * 登录系统成功时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logLoginSuccess(String content, UserModel userModel);

    /**
     * 登录系统失败时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logLoginError(String content, UserModel userModel);

    /**
     * 登出系统成功时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logLogout(String content, UserModel userModel);

    /**
     * 添加数据时记录日志.
     *
     * @param content .
     * @param userModel 操作者
     */
    public void logAdd(String content, UserModel userModel);

    /**
     * 更新数据时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logEdit(String content, UserModel userModel);

    /**
     * 删除数据时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logDelete(String content, UserModel userModel);

    /**
     * 查询数据时记录日志.
     *
     * @param content
     * @param userModel 操作者
     */
    public void logQuery(String content, UserModel userModel);
}
