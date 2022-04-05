package com.xpay.demo.web.excel.utils;

import java.io.Serializable;
import java.util.List;

/**
 * Excel表格上传下载的DTO
 */
public class ExcelData<T> implements Serializable {
    private static final long serialVersionUID = 444401723665748239L;
    // 表头
    private List<String> titles;
    // 数据
    private List<List<T>> rows;
    // 页签名称
    private String sheetName;
    // 文件名
    private String fileName;

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public List<List<T>> getRows() {
        return rows;
    }

    public void setRows(List<List<T>> rows) {
        this.rows = rows;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}