package com.xpay.sdktest;

import com.xpay.sdk.api.entity.*;
import com.xpay.sdk.api.enums.MediaType;
import com.xpay.sdk.api.enums.SignType;
import com.xpay.sdk.api.utils.*;

import java.util.*;

public class TestFileUtil {
    public static String PATH = "/backend-flux";
//    public static String PATH = "/backend-mvc";


    public static void main(String[] args) {
        final SecretKey key = new SecretKey();
        key.setMchPriKey(Keys.mchPrivateKey);//商户私钥
        key.setPlatPubKey(Keys.platPublicKey);//平台公钥

//        uploadFile(key);

        download(key, "D:\\download\\", "Elasticsearch服务器开发（第2版）.pdf");
    }

    private static void uploadFile(SecretKey secretKey) {
        String url = "127.0.0.1:8099" + PATH;

        String fileDir = "D:\\upload\\";
        Map<String, String> fileMap = new LinkedHashMap<>();
        fileMap.put("Elasticsearch服务器开发（第2版）.pdf", MediaType.APPLICATION_PDF.getValue());
        fileMap.put("ElasticSearch文档.docx", MediaType.APPLICATION_WORD_07.getValue());
        fileMap.put("test_image_upload.jpg", MediaType.IMAGE_JPEG.getValue());
        fileMap.put("阿里巴巴Java开发手册.zip", MediaType.APPLICATION_ZIP.getValue());
        fileMap.put("03版excel表格.xls", MediaType.APPLICATION_EXCEL_O3.getValue());
        fileMap.put("07版excel表格.xlsx", MediaType.APPLICATION_EXCEL_07.getValue());

        List<FileInfo> fileInfos = new ArrayList<>();
        for(Map.Entry<String, String> entry : fileMap.entrySet()) {
            FileInfo info = new FileInfo();
            info.setFilename(entry.getKey());
            info.setContentType(entry.getValue());
            info.setData(FileUtil.readFile(fileDir + info.getFilename()));
            fileInfos.add(info);
            System.out.println(info.getFilename() + " 文件大小为：" + info.getData().length);
        }

        Map<String, String> extras = new HashMap<>();
        extras.put("key_01", "value_01");
        extras.put("key_02", "value_02");

        FileRequest fileParam = new FileRequest();
        fileParam.setMchNo(Keys.mchNo);
        fileParam.setSignType(SignType.RSA.getValue());
        fileParam.setMethod("demo.upload");
        fileParam.setVersion("1.0");
        fileParam.setRandStr(RandomUtil.get32LenStr());
        fileParam.setTimestamp(String.valueOf(System.currentTimeMillis()));
        fileParam.setExtras(JsonUtil.toJson(extras));
        fileParam.setFiles(fileInfos);
        fileParam.setSecKey(RandomUtil.get16LenStr());//测试对于密钥的加解密

        Response response = RequestUtil.doFileRequest(url, fileParam, secretKey);
        System.out.println("Response : " + JsonUtil.toJson(response));
    }

    public static void download(SecretKey key, String dir, String filename){
        String url = "127.0.0.1:8099" + PATH;

        Request request = new Request();
        request.setMchNo(Keys.mchNo);
        request.setMethod("demo.download");
        request.setVersion("1.0");
        request.setSignType(SignType.RSA.getValue());
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setData(filename);

        HttpUtil.Response response = RequestUtil.downloadFile(url, request, key, 5000);
        System.out.println("statusCode : " + response.getCode() + ", headers: " + JsonUtil.toJson(response.getHeaders()));
        if(response.isSuccessful()) {
            FileUtil.writeFile(dir, filename, response.getBody());
        }
    }
}
