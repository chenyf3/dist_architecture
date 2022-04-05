package com.xpay.facade.user.dto;

import java.io.Serializable;

/**
 * 商户功能表
 */
public class PortalAuthDto implements Serializable {
	private static final long serialVersionUID = 1L;

	//columns START
	/**
	 * 功能ID
	 */
	private Long id;
	/**
	 * VERSION
	 */
	private Integer version;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 序号
	 */
	private String number;
	/**
	 * 父节点，一级菜单为0
	 */
	private Long parentId;
	/**
	 * 权限标识
	 */
	private String permissionFlag;
	/**
	 * 1:菜单 2:功能
	 */
	private Integer authType;
	/**
	 * 菜单URL
	 */
	private String url;
	/**
	 * 图标
	 */
	private String icon;
	//columns END

	//父节点，非数据库字段
	private PortalAuthDto parent;

	/**
	 * 功能ID
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 功能ID
	 */
	public Long getId() {
		return this.id;
	}
	/**
	 * VERSION
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
	/**
	 * VERSION
	 */
	public Integer getVersion() {
		return this.version;
	}
	/**
	 * 创建时间
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * 创建时间
	 */
	public java.util.Date getCreateTime() {
		return this.createTime;
	}
	/**
	 * 名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 名称
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * 序号
	 */
	public void setNumber(String number) {
		this.number = number;
	}
	/**
	 * 序号
	 */
	public String getNumber() {
		return this.number;
	}
	/**
	 * 父节点，一级菜单为0
	 */
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	/**
	 * 父节点，一级菜单为0
	 */
	public Long getParentId() {
		return this.parentId;
	}
	/**
	 * 权限标识
	 */
	public void setPermissionFlag(String permissionFlag) {
		this.permissionFlag = permissionFlag;
	}
	/**
	 * 权限标识
	 */
	public String getPermissionFlag() {
		return this.permissionFlag;
	}
	/**
	 * 1:菜单 2:功能
	 */
	public void setAuthType(Integer authType) {
		this.authType = authType;
	}
	/**
	 * 1:菜单 2:功能
	 */
	public Integer getAuthType() {
		return this.authType;
	}
	/**
	 * 菜单URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 菜单URL
	 */
	public String getUrl() {
		return this.url;
	}
	/**
	 * 图标
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}
	/**
	 * 图标
	 */
	public String getIcon() {
		return this.icon;
	}

	public PortalAuthDto getParent() {
		return parent;
	}

	public void setParent(PortalAuthDto parent) {
		this.parent = parent;
	}
}
