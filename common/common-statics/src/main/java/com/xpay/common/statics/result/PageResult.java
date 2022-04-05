package com.xpay.common.statics.result;

import com.xpay.common.statics.query.PageQuery;

/**
 * 分页查询返回结果
 * @author chenyf
 */
public class PageResult<T> extends BaseResult<T> {
    private static final long serialVersionUID = -189498483727889L;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalRecord;

    /**
     * elasticsearch遍历扫描时的scrollId
     */
    private String scrollId;

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(Long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public static <T> PageResult<T> newInstance(T data, PageQuery pageQuery){
        return PageResult.newInstance(data, pageQuery, (long)BaseResult.calcDataLength(data));
    }

    public static <T> PageResult<T> newInstance(T data, PageQuery pageQuery, Long totalRecord){
        PageResult<T> pageResult = new PageResult<T>();
        pageResult.setCurrentPage(pageQuery.getCurrentPage());
        pageResult.setPageSize(pageQuery.getPageSize());
        pageResult.setData(data);
        pageResult.setTotalRecord(totalRecord);
        return pageResult;
    }

    public static <T> PageResult<T> newInstance(T data, Integer pageCurrent, Integer pageSize){
        return PageResult.newInstance(data, pageCurrent, pageSize, (long)BaseResult.calcDataLength(data));
    }

    public static <T> PageResult<T> newInstance(T data, Integer pageCurrent, Integer pageSize, Long totalRecord){
        PageResult<T> pageResult = new PageResult<T>();
        pageResult.setCurrentPage(pageCurrent);
        pageResult.setPageSize(pageSize);
        pageResult.setData(data);
        pageResult.setTotalRecord(totalRecord);
        return pageResult;
    }

    public static <T> PageResult<T> newInstance(T data, PageResult other){
        PageResult<T> pageResult = new PageResult<T>();
        pageResult.setCurrentPage(other.getCurrentPage());
        pageResult.setPageSize(other.getPageSize());
        pageResult.setTotalRecord(other.getTotalRecord());
        pageResult.setScrollId(other.getScrollId());
        pageResult.setData(data);
        return pageResult;
    }
}
