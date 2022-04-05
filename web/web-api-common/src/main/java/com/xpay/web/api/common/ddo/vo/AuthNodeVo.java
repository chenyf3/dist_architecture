package com.xpay.web.api.common.ddo.vo;

import java.util.List;

/**
 * 权限节点VO
 */
public class AuthNodeVo {

    /**
     * id
     */
    private Long id;

    /**
     * 父id
     */
    private Long parentId;

    /**
     * 功能名称
     */
    private String name;

    /**
     * 序号
     */
    private String number;

    /**
     * 权限标识
     */
    private String permissionFlag;

    /**
     * 功能类型
     */
    private Integer authType;

    /**
     * url
     */
    private String url;

    private String icon;

    /**
     * 子菜单项
     */
    private List<AuthNodeVo> children;

    /**
     * 子操作项
     */
    private List<AuthNodeVo> actionChildren;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public List<AuthNodeVo> getChildren() {
        return children;
    }

    public void setChildren(List<AuthNodeVo> children) {
        this.children = children;
    }

    public List<AuthNodeVo> getActionChildren() {
        return actionChildren;
    }

    public void setActionChildren(List<AuthNodeVo> actionChildren) {
        this.actionChildren = actionChildren;
    }
}
