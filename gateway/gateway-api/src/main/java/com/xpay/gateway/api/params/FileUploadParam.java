package com.xpay.gateway.api.params;

import java.util.*;

/**
 * 文件上传的参数
 */
public class FileUploadParam extends RequestParam {
    private String hash; //文件的hash值，如：md5值，上传多个文件时用英文的逗号分割
    private String extras; //额外的参数(如有需要，可以使用)

    /**------------- 以下是临时存放的字段，只在网关内部使用 ---------------*/
    private byte[] oriBody; //源请求体
    private long fileLength; //所有文件的字节总长度
    private List<FileInfo> files;//文件内容

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public byte[] getOriBody() {
        return oriBody;
    }

    public FileUploadParam setOriBody(byte[] oriBody) {
        this.oriBody = oriBody;
        return this;
    }

    public long getFileLength() {
        return fileLength;
    }

    public FileUploadParam setFileLength(long fileLength) {
        this.fileLength = fileLength;
        return this;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    public Map<String, String> getTextParamSorted() {
        Map<String, String> map = new TreeMap<>();
        map.put("method", getMethod());
        map.put("version", getVersion());
        map.put("randStr", getRandStr());
        map.put("signType", getSignType());
        map.put("mchNo", getMchNo());
        map.put("secKey", getSecKey());
        map.put("timestamp", getTimestamp());
        map.put("hash", getHash());
        map.put("extras", getExtras());
        return map;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"method\":").append(getMethod()).append(", ")
                .append("\"version\":").append(getVersion()).append(", ")
                .append("\"randStr\":").append(getRandStr()).append(", ")
                .append("\"signType\":").append(getSignType()).append(", ")
                .append("\"mchNo\":").append(getMchNo()).append(", ")
                .append("\"secKey\":").append(getSecKey()).append(", ")
                .append("\"timestamp\":").append(getTimestamp()).append(", ")
                .append("\"hash\":").append(getHash()).append(", ")
                .append("\"extras\":").append(getExtras()).append(", ")
                .append("\"fileLength\":").append(getFileLength()).append(", ");

        builder.append("\"files:\":").append("[");
        if(files != null) {
            for(int i=0; i<files.size(); i++){
                FileInfo fileInfo = files.get(i);
                builder.append("{")
                        .append("\"filename\":").append(fileInfo.getFilename()).append(", ")
                        .append("\"length\":").append(fileInfo.getLength()).append(", ")
                        .append("\"contentType\":").append(fileInfo.getContentType())
                        .append("}");
                if(i < files.size()-1){
                    builder.append(",");
                }
            }
        }
        builder.append("]").append("}");
        return builder.toString();
    }

    public void clearTempField(){
        super.clearTempField();
        this.oriBody = null;
        this.files = null;
    }

    public static class FileInfo {
        private String filename;
        private String contentType;
        private byte[] data; //文件内容
        private int length;//文件内容的字节长度

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

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }
}
