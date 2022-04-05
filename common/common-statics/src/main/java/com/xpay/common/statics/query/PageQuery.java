package com.xpay.common.statics.query;

import java.io.Serializable;

/**
 * 分页查询的参数
 * @author： chenyf
 */
public class PageQuery implements Serializable {
    private static final long serialVersionUID = 6297178964005032338L;
    /**
     * 当前页数，默认为1，必须设置默认值，否则分页查询时容易报空指针异常
     */
    private int currentPage = 1;
    /**
     * 每页记录数，默认为20，必须设置默认值，否则分页查询时容易报空指针异常
     */
    private int pageSize = 20;
    /**
     * 是否需要总记录数，默认为true
     */
    private boolean isNeedTotalRecord = true;
    /**
     * 总记录数，根据查询条件查询出来的总记录数
     */
    private long totalRecord;
    /**
     * 排序字段及排序方向
     */
    private String sortColumns;

    public PageQuery(){
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

    public boolean isNeedTotalRecord() {
        return this.isNeedTotalRecord;
    }

    public void setIsNeedTotalRecord(boolean isNeedTotalRecord) {
        this.isNeedTotalRecord = isNeedTotalRecord;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public String getSortColumns() {
        return sortColumns;
    }

    public void setSortColumns(String sortColumns) {
        this.sortColumns = sortColumns;
    }

    public static PageQuery newInstance(int currentPage, int pageSize){
        PageQuery pageParam = new PageQuery();
        pageParam.setCurrentPage(currentPage);
        pageParam.setPageSize(pageSize);
        return pageParam;
    }

    public static PageQuery newInstance(int currentPage, int pageSize, String sortColumns){
        PageQuery pageParam = new PageQuery();
        pageParam.setCurrentPage(currentPage);
        pageParam.setPageSize(pageSize);
        pageParam.setSortColumns(sortColumns);
        return pageParam;
    }
}
