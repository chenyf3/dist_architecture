package com.xpay.demo.web.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.xpay.common.statics.exception.BizException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * excel文件的读取和写入
 * @author chenyf
 */
public class ExcelUtil {
    final static String DEFAULT_SHEET_NAME = "Sheet1";
    final static String DEFAULT_CHARSET = "UTF-8";

    /**
     * 读文件
     * @param file      上传的文件
     * @param clazz     文件读取后转换为哪个Class的实例对象（该Class的属性需要添加 @ExcelProperty 注解）
     * @param <T>       读入数据的类型
     * @return
     */
    public static <T> List<T> read(MultipartFile file, Class<T> clazz) {
        try {
            ModelExcelListener<T> listener = new ModelExcelListener<>();
            EasyExcel.read(file.getInputStream(), clazz, listener)
                    .sheet()
                    .doRead();
            return listener.getDataList();
        } catch (Exception e) {
            throw new BizException("文件读取异常", e);
        }
    }

    /**
     * 读文件，并指定有多少行头（easyExcel默认1）
     * @param file      上传的文件
     * @param clazz     文件读取后转换为哪个Class的实例对象（该Class的属性需要添加 @ExcelProperty 注解）
     * @param headRows  行头的数量
     * @param <T>       读入数据的类型
     * @return
     */
    public static <T> List<T> read(MultipartFile file, Class<T> clazz, int headRows) {
        try {
            ModelExcelListener<T> listener = new ModelExcelListener<>();
            EasyExcel.read(file.getInputStream(), clazz, listener)
                    .headRowNumber(headRows)
                    .sheet()
                    .doRead();
            return listener.getDataList();
        } catch (Exception e) {
            throw new BizException("文件读取异常", e);
        }
    }

    /**
     * 读文件
     * @param file      上传的文件
     * @param sheetName excel中的sheet名称
     * @param clazz     文件读取后转换为哪个Class的实例对象（该Class的属性需要添加 @ExcelProperty 注解）
     * @param <T>       写入数据的类型
     * @return
     */
    public static <T> List<T> read(MultipartFile file, String sheetName, Class<T> clazz) {
        try {
            ModelExcelListener<T> listener = new ModelExcelListener<>();
            EasyExcel.read(file.getInputStream(), clazz, listener)
                    .sheet(sheetName)
                    .doRead();
            return listener.getDataList();
        } catch (Exception e) {
            throw new BizException("文件读取异常", e);
        }
    }

    /**
     * 写文件，此方法适用于 data 中的数据是有使用 @ExcelProperty 来定义的模版
     * @param response      响应
     * @param sheetName     excel中的sheet名称
     * @param data          需要写入的数据
     * @param fileName      响应给前端下载的文件名
     * @param <T>           写入数据的类型，类中的属性需要使用 @ExcelProperty 注解
     */
    public static <T> void write(HttpServletResponse response, String sheetName, List<T> data, String fileName) {
        if (sheetName == null || "".equals(sheetName.trim())) {
            sheetName = DEFAULT_SHEET_NAME;
        }
        try {
            setResponseHeader(response, fileName);

            Class clazz = null;
            if (data != null && !data.isEmpty()) {
                clazz = data.get(0).getClass();
            }
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception e) {
            throw new BizException("文件写入异常", e);
        }
    }

    /**
     * 写文件
     * @param response      响应
     * @param sheetName     excel中的sheet名称
     * @param head          表头
     * @param data          需要写入的数据
     * @param fileName      响应给前端下载的文件名
     * @param <T>           写入数据的类型Class
     */
    public static <T> void write(HttpServletResponse response, String sheetName, List<String> head, List<T> data, String fileName) {
        if (head == null || head.isEmpty()) {
            throw new BizException("表头不能为空");
        }
        if (sheetName == null || "".equals(sheetName.trim())) {
            sheetName = DEFAULT_SHEET_NAME;
        }
        if (data == null) {
            data = new ArrayList<>();
        }

        try {
            setResponseHeader(response, fileName);

            List<List<String>> headList = new ArrayList<>();
            head.forEach(h -> headList.add(Collections.singletonList(h)));

            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream());
            builder.head(headList);
            builder.sheet(sheetName).doWrite(data);
        } catch(Exception e) {
            throw new BizException("文件写入异常", e);
        }
    }

    /**
     * 写文件
     * @param response      响应
     * @param excelData     需要写入响应的数据
     * @param <T>           写入数据的类型Class
     */
    public static <T> void write(HttpServletResponse response, ExcelData<T> excelData) {
        if (excelData == null) {
            throw new BizException("表头不能为空");
        } else if (excelData.getTitles() == null || excelData.getTitles().isEmpty()) {
            throw new BizException("表头不能为空");
        }
        if (excelData.getSheetName() == null || "".equals(excelData.getSheetName().trim())) {
            excelData.setSheetName(DEFAULT_SHEET_NAME);
        }
        if (excelData.getRows() == null) {
            excelData.setRows(new ArrayList<>());
        }

        try {
            setResponseHeader(response, excelData.getFileName());

            List<List<String>> headList = new ArrayList<>();
            excelData.getTitles().forEach(h -> headList.add(Collections.singletonList(h)));

            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream());
            builder.head(headList);
            builder.sheet(excelData.getSheetName()).doWrite(excelData.getRows());
        } catch (Exception e) {
            throw new BizException("文件写入异常", e);
        }
    }

    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding(DEFAULT_CHARSET);
        try {
            fileName = URLEncoder.encode(fileName + ".xlsx", DEFAULT_CHARSET);//URLEncoder.encode可以防止中文乱码
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        } catch (UnsupportedEncodingException e) {
            throw new BizException("", e);
        }
    }

    /**
     * 模型解析监听器 -- 每解析一行会回调invoke()方法，整个excel解析结束会执行doAfterAllAnalysed()方法
     */
    private static class ModelExcelListener<E> extends AnalysisEventListener<E> {
        private List<E> dataList = new ArrayList<>();

        @Override
        public void invoke(E object, AnalysisContext context) {
            dataList.add(object);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }

        public List<E> getDataList() {
            return dataList;
        }

        @SuppressWarnings("unused")
        public void setDataList(List<E> dataList) {
            this.dataList = dataList;
        }
    }
}
