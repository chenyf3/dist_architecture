package com.xpay.sdk.api.enums;

public enum MediaType {
    APPLICATION_JSON_UTF8("application/json;charset=UTF-8"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_OCTET_STREAM("application/octet-stream"), //表示通用的二进制文件
    APPLICATION_PDF("application/pdf"),
    APPLICATION_XML("application/xml"),
    APPLICATION_ZIP("application/zip"),
    APPLICATION_RAR("application/x-rar-compressed"),
    APPLICATION_WORD_O3("application/msword"), //.doc文件，03版的word文档
    APPLICATION_WORD_07("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), //.docx文件，07版的word文档
    APPLICATION_EXCEL_O3("application/vnd.ms-excel"), //.xls文件，03版的excel表格
    APPLICATION_EXCEL_07("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), //.xlsx文件，07版的excel表格
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_GIF("image/gif"),
    TEXT_PLAIN("text/plain"),
    TEXT_CSV("text/csv"), //.csv文件
    ;


    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private MediaType(String value){
        this.value = value;
    }
}
