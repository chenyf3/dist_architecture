package com.xpay.demo.backend.flux;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.dto.RequestDto;
import com.xpay.common.api.dto.file.FileInfo;
import com.xpay.common.api.dto.file.FluxFileDto;
import com.xpay.common.api.dto.ResponseDto;
import com.xpay.common.api.utils.FileUtil;
import com.xpay.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class FileController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "upload")
    public Mono<ResponseDto<String>> upload(FluxFileDto fileDto) {
        List<FileInfo> fileInfos = fileDto.getFileInfos();
        fileDto.setFileInfos(null);//清空，避免打印日志时内容过长

        logger.info("FileUploadDto = {}", JsonUtil.toJson(fileDto));

        fileInfos.forEach(fileInfo -> {
            //do something：把文件保存到本地文件或者上传到文件服务器等等，这里示例就只打印一下文件名和文件大小
            logger.info("filename={} fileSize={}(byte) mediaType={}", fileInfo.getFilename(), fileInfo.getData().length, fileInfo.getContentType());

            FileUtil.writeFile("D:\\serverRec\\", fileInfo.getFilename(), fileInfo.getData());
        });

        ResponseDto response = ResponseDto.success(fileDto.getMchNo(), fileDto.getSignType(), "success");
        return Mono.just(response);
    }

    @RequestMapping(value = "download")
    public ResponseEntity<byte[]> download(@RequestBody RequestDto<String> request) throws Exception {
        String filename = request.getData();
        String fileDir = "D:\\serverRec\\";
        byte[] fileBytes = FileUtil.readFile(fileDir + filename);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        //此处是测试方法，就简单点，根据文件名后缀来设置文件的媒体类型了
        if(filename.lastIndexOf(".jpg") > 0 || filename.lastIndexOf(".jpeg") > 0) {
            contentType = MediaType.IMAGE_JPEG;
        }else if(filename.lastIndexOf(".png") > 0) {
            contentType = MediaType.IMAGE_PNG;
        }else if(filename.lastIndexOf(".pdf") > 0) {
            contentType = MediaType.APPLICATION_PDF;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.set(HttpHeaderKey.RESP_ORIGINAL_BODY_KEY, "true");//设置返回原始内容的响应头

        String downloadFileName = URLEncoder.encode(filename,"UTF-8");//防止中文文件名不显示
        headers.setContentDispositionFormData("attachment", downloadFileName);

        logger.info("filename:{} contentType: {} bodyLength:{}", filename, contentType, fileBytes.length);

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}
