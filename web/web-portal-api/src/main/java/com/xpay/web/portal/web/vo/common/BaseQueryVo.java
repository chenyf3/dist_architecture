package com.xpay.web.portal.web.vo.common;

import jakarta.validation.constraints.NotNull;

/**
 * 查询基类 BaseQueryDTO
 */
public class BaseQueryVo {
	/**
	 * 创建时间开始
	 */
	private String createTimeStart;
	/**
	 * 创建时间结束
	 */
	private String createTimeEnd;

	@NotNull(message = "当前页不能为空")
	private int currentPage;

	@NotNull(message = "每页记录数不能为空")
	private int pageSize;

	public String getCreateTimeStart() {
		return createTimeStart;
	}

	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeEnd() {
		return createTimeEnd;
	}

	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
}
