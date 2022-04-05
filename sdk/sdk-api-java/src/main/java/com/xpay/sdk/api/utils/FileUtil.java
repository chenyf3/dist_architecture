package com.xpay.sdk.api.utils;

import java.io.*;

public class FileUtil {

    public static byte[] readFile(String filePath) {
        ByteArrayOutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            fileInputStream = new FileInputStream(filePath);
            bufferedInputStream = new BufferedInputStream(fileInputStream);

            int len = -1;
            // 定义一个容量来盛放数据
            byte[] buf = new byte[1024];
            while ((len = bufferedInputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("文件读取异常 filePath: " + filePath, e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
            }
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception e) {
            }
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void writeFile(String dir, String fileName, byte[] fileBytes){
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
