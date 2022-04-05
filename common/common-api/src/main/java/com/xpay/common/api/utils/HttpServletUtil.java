package com.xpay.common.api.utils;

import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpServletUtil {

    public static String readBodyStr(HttpServletRequest request) {
        if(request instanceof ContentCachingRequestWrapper){
            byte[] byteData =  ((ContentCachingRequestWrapper)request).getContentAsByteArray();
            return new String(byteData, StandardCharsets.UTF_8);
        }else{
            return readStrOnce(request);
        }
    }

    public static byte[] readBodyByte(HttpServletRequest request){
        if(request instanceof ContentCachingRequestWrapper){
            return ((ContentCachingRequestWrapper)request).getContentAsByteArray();
        }else{
            return readByteOnce(request);
        }
    }

    /**
     * 只能读取一次
     * @param request
     * @return
     */
    private static String readStrOnce(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private static byte[] readByteOnce(HttpServletRequest request) {
        int len = request.getContentLength();
        byte[] buffer = new byte[len];
        ServletInputStream in = null;

        try {
            in = request.getInputStream();
            in.read(buffer, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }
}
