package com.xpay.common.api.dto.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用WebMvc接收文件上传时的参数
 */
public class MvcFileDto extends FileUploadDto {

    private List<MultipartFile> files;//上传的文件(此字段的值将由webMvc框架注入)，为避免误操作，此字段不设置getter方法

    private List<FileInfo> fileInfos = new ArrayList<>();//文件内容(从files属性中读取到)

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public void setFileInfos(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    public synchronized List<FileInfo> getFileInfos() {
        if(this.files == null || !this.fileInfos.isEmpty()) {
            return fileInfos;
        }

        for (MultipartFile part : this.files) {
            FileInfo info = new FileInfo();
            info.setContentType(part.getContentType());
            info.setFilename(part.getOriginalFilename());
            try {
                info.setData(part.getBytes());
                info.setLength(info.getData().length);
            } catch (Exception e) {
                this.files = null;//置空，只能被读取一次
                throw new RuntimeException(e);
            }
            this.fileInfos.add(info);
        }
        this.files = null;//置空，只能被读取一次
        return this.fileInfos;
    }
}
