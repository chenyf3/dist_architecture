package com.xpay.demo.backend.mvc;

import com.xpay.common.api.dto.file.FileInfo;
import com.xpay.common.api.dto.file.MvcFileDto;
import com.xpay.common.api.dto.ResponseDto;
import com.xpay.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class FileController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "upload")
    public ResponseDto<String> upload(MvcFileDto fileDto) {
        List<FileInfo> fileInfos = fileDto.getFileInfos();
        fileDto.setFileInfos(null);//清空，避免打印日志时内容过长

        logger.info("FileUploadDto = {}", JsonUtil.toJson(fileDto));

        fileInfos.forEach(fileInfo -> {
            //do something：把文件保存到本地文件或者上传到文件服务器等等，这里示例就只打印一下文件名和文件大小
            logger.info("filename={} fileSize={}(byte) mediaType={}", fileInfo.getFilename(), fileInfo.getData().length, fileInfo.getContentType());

            writeFile("D:\\serverRec\\", fileInfo.getFilename(), fileInfo.getData());
        });

        ResponseDto response = ResponseDto.success(fileDto.getMchNo(), fileDto.getSignType(), "success");
        return response;
    }

    private void writeFile(String dir, String fileName, byte[] fileBytes){
        FileOutputStream fos = null;
        try {
            File dirFile = new File(dir);
            dirFile.mkdirs();

            String fullPath = dir + fileName;
            File writeFile = new File(fullPath);
            if(! writeFile.exists()){
                writeFile.createNewFile();
            }

            fos = new FileOutputStream(writeFile);
            fos.write(fileBytes);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(fos != null){
                try{
                    fos.close();
                }catch (Exception e){
                }
            }
        }
    }
}
