package com.xpay.web.api.common.ddo.dto;

import com.xpay.common.statics.query.PageQuery;

/**
 * Author: Cmf
 * Date: 2020.1.20
 * Time: 15:43
 * Description: 操作员查询VO
 */
public class UserQueryDto extends PageQuery {
    private int currentPage;
    private int pageSize;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 姓名
     */
    private String realName;
    /**
     * 手机号
     */
    private String mobileNo;
    /**
     * 状态
     */
    private Integer status;
    private Integer type;
    private String mchNo;

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }
}
