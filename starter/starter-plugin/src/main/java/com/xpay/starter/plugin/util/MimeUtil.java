package com.xpay.starter.plugin.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MimeUtil {
    private final static Map<String, String> FILE_TYPE_MAP;
    private final static String DEFAULT_TYPE = "application/octet-stream";

    static {
        FILE_TYPE_MAP = new ConcurrentHashMap<>();
        //text文件
        FILE_TYPE_MAP.put("75736167", "text/plain"); //txt文件
        FILE_TYPE_MAP.put("002a2a2a2020496e73", "text/plain"); //txt文件

        //image类型
        FILE_TYPE_MAP.put("ffd8ff", "image/jpeg"); // JPEG (jpg)
        FILE_TYPE_MAP.put("89504e47", "image/png"); // PNG (png)
        FILE_TYPE_MAP.put("4749463837", "image/gif"); // GIF (gif)
        FILE_TYPE_MAP.put("4749463839", "image/gif"); // GIF (gif)
        FILE_TYPE_MAP.put("49492a00227105008037", "image/tiff"); // TIFF (tif)
        FILE_TYPE_MAP.put("4d4d002a", "image/tiff"); // TIFF (tif)
        FILE_TYPE_MAP.put("4d4d002b", "image/tiff"); // TIFF (tif)
        FILE_TYPE_MAP.put("424d228c010000000000", "image/bmp"); // 16色位图(bmp)
        FILE_TYPE_MAP.put("424d8240090000000000", "image/bmp"); // 24位位图(bmp)
        FILE_TYPE_MAP.put("424d8e1b030000000000", "image/bmp"); // 256色位图(bmp)
        FILE_TYPE_MAP.put("41433130313500000000", "image/vnd.dwg"); // CAD (dwg)
        FILE_TYPE_MAP.put("0a020101", "image/x-pcx"); // pcx
        FILE_TYPE_MAP.put("0a030101", "image/x-pcx"); // pcx
        FILE_TYPE_MAP.put("0a050101", "image/x-pcx"); // pcx
        FILE_TYPE_MAP.put("38425053", "image/vnd.adobe.photoshop"); // Photoshop (psd)

        //audio类型
        FILE_TYPE_MAP.put("49443303000000002176", "audio/mpeg");//mp3
        FILE_TYPE_MAP.put("00000020667479706", "audio/mp4");
        FILE_TYPE_MAP.put("52494646e27807005741", "audio/x-wav"); // Wave (wav)
        FILE_TYPE_MAP.put("4d546864000000060001", "audio/midi"); // mid、midi、rmi

        //video类型
        FILE_TYPE_MAP.put("464c5601050000000900", "video/x-flv"); // flv与f4v相同
        FILE_TYPE_MAP.put("000000146674797069736f6d", "video/mp4");
        FILE_TYPE_MAP.put("0000001866747970", "video/mp4");
        FILE_TYPE_MAP.put("0000001c66747970", "video/mp4");
        FILE_TYPE_MAP.put("000001ba210001000180", "video/mpeg"); //mpg
        FILE_TYPE_MAP.put("000001b3", "video/mpeg"); //mpg
        FILE_TYPE_MAP.put("3026b2758e66cf11", "video/x-ms-wmv"); // wmv与asf相同
        FILE_TYPE_MAP.put("52494646d07d60074156", "video/x-msvideo"); //avi

        //application类型
        FILE_TYPE_MAP.put("504B030414000100", "application/zip");//zip
        FILE_TYPE_MAP.put("526172211a0700", "application/x-rar-compressed");// WinRAR
        FILE_TYPE_MAP.put("255044462d312e", "application/pdf"); // Adobe Acrobat (pdf)
        FILE_TYPE_MAP.put("3C3F786D6C", "application/xml"); // xml文件
        FILE_TYPE_MAP.put("68746D6C3E", "application/html"); // html文件
        FILE_TYPE_MAP.put("d0cf11e0", "application/vnd.ms-excel"); //xls、ppt、doc
        FILE_TYPE_MAP.put("504b030414000600", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//docx、xlsx、pptx
        FILE_TYPE_MAP.put("5374616E64617264204A", "application/x-msaccess"); // MS Access (mdb)
        FILE_TYPE_MAP.put("000100005374616e6461", "application/x-msaccess"); // MS Access (mdb)
        FILE_TYPE_MAP.put("7b5c727466", "application/rtf"); // Rich Text Format (rtf)
        FILE_TYPE_MAP.put("2e524d46000000120001", "application/vnd.rn-realmedia-vbr"); // rmvb/rm相同
        FILE_TYPE_MAP.put("504B03040a000000", "application/java-archive");//jar
        FILE_TYPE_MAP.put("504B030414000800", "application/java-archive");//jar
        FILE_TYPE_MAP.put("4d5a9000030000000400", "application/x-msdownload");// exe 可执行文件
    }

    /**
     * 根据文件的字节数组内容推断该文件的媒体类型，原理就是通过文件的前28个字节来判断的
     * @param fileData
     * @return
     */
    public static String getMimeType(byte[] fileData) {
        String head = Utils.read28UpperHex(fileData);
        for (Map.Entry<String, String> fileTypeEntry : FILE_TYPE_MAP.entrySet()) {
            if (Utils.startWithIgnoreCase(head, fileTypeEntry.getKey())) {
                return fileTypeEntry.getValue();
            }
        }
        return DEFAULT_TYPE;
    }
}


