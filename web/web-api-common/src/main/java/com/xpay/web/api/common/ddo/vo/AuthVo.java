package com.xpay.web.api.common.ddo.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限VO
 */
public class AuthVo implements Serializable {
    private Long id;
    private Integer version = 0;
    private Date createTime = new Date();
    private String name;                    // 名称NAME
    private String number;                  // 编号NUMBER
    private Long parentId;                  // 父节点id
    private String permissionFlag;          // 权限标识
    private Integer authType;           // MENU_TYPE ,ACTION_TYPE
    private String url;                     // 后端API地址
    private String icon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPermissionFlag() {
        return permissionFlag;
    }

    public void setPermissionFlag(String permissionFlag) {
        this.permissionFlag = permissionFlag;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
