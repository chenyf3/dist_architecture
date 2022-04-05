package com.xpay.common.api.dto.file;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用WebFlux接收文件上传时的参数
 */
public class FluxFileDto extends FileUploadDto {

    private List<FilePart> files;//上传的文件(此字段的值将由webFlux框架注入)，为避免误操作，此字段不设置getter方法

    private List<FileInfo> fileInfos = new ArrayList<>();//文件内容(从files属性中读取到)

    public void setFiles(List<FilePart> files) {
        this.files = files;
    }

    public void setFileInfos(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    public synchronized List<FileInfo> getFileInfos(){
        if(this.files == null || !this.fileInfos.isEmpty()) {
            return fileInfos;
        }

        for(FilePart part : this.files){
            FileInfo info = new FileInfo();
            info.setContentType(part.headers().getContentType().toString());
            info.setFilename(part.filename());

            DataBufferUtils.join(part.content()).subscribe(dataBuffer -> {
                byte[] fileBytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(fileBytes);
                DataBufferUtils.release(dataBuffer); //释放资源
                info.setData(fileBytes);
                info.setLength(fileBytes.length);
                this.fileInfos.add(info);
            });
        }
        this.files = null;//置空，只能被读取一次
        return this.fileInfos;
    }
}
