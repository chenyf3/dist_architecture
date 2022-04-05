package com.xpay.common.api.dto.file;

/**
 * 文件上传相关信息
 */
public class FileInfo {
    private String filename;
    private String contentType;
    private byte[] data; //文件内容
    private long length; //文件内容的字节长度

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
