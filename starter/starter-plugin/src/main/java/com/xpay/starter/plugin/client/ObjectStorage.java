package com.xpay.starter.plugin.client;

import com.xpay.starter.plugin.ddo.BucketInfo;
import com.xpay.starter.plugin.ddo.ClusterInfo;
import com.xpay.starter.plugin.ddo.ObjectInfo;
import com.xpay.starter.plugin.util.HexUtil;

import java.util.List;
import java.util.Map;

public interface ObjectStorage {
    String BUCKET_SEPARATOR = "-";
    String OBJECT_SEPARATOR = "_";

    String makeBucket(String bucketFullName);

    public boolean bucketExists(String bucketFullName);

    public BucketInfo getBucketInfo(String bucketName);

    public List<String> listBucketNames();

    public List<BucketInfo> listBuckets();

    public boolean removeBucket(String bucketFullName);

    public boolean hasObject(String bucketFullName);

    public List<String> listObjectNames(String bucketFullName);

    public List<String> listObjectNames(String bucketFullName, String startAfter, int limit);

    public List<ObjectInfo> listObjectInfos(String bucketFullName, String startAfter, int limit);

    public String putObject(String bucketFullName, String oriFileName, byte[] data);

    public String putObject(String bucketFullName, String oriFileName, byte[] data, String contentType);

    public String putObject(String bucketFullName, String objectFullName, byte[] data, String contentType,
                            Map<String, String> headers, Map<String, String> metaData);

    public byte[] getObject(String objectFullName);

    public byte[] getObject(String objectFullName, long offset, long length);

    public ObjectInfo getObjectInfo(String objectFullName, String version);

    public boolean removeObject(String objectFullName);

    public boolean removeObject(List<String> objectNames);

    public String getObjectURL(String objectFullName, Integer expireSec);

    public String getBucketFullName(String bucketName);

    default String getObjectFullName(String bucketFullName, String objectName) {
        if(objectName.startsWith(bucketFullName)){
            return objectName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bucketFullName)
                .append(OBJECT_SEPARATOR)
                .append(objectName);
        return sb.toString();
    }

    default String getObjectFullName(String bucketFullName, byte[] data, String oriFileName) {
        String fileName = HexUtil.encodeHexMd5(data);//获取当前数据的md5值
        String suffix = oriFileName.substring(oriFileName.lastIndexOf("."));//取得文件后缀
        StringBuilder sb = new StringBuilder();
        sb.append(bucketFullName)
                .append(OBJECT_SEPARATOR)
                .append(fileName)
                .append(suffix);
        return sb.toString();
    }

    /**
     * 根据原文件名截取文件后缀
     * @param oriFileName
     * @return
     */
    default String getFileNameSuffix(String oriFileName){
        int index = oriFileName.lastIndexOf(".");
        if(index > 0){
            return oriFileName.substring(index);//取得文件后缀
        }else{
            return "";
        }
    }

    public ClusterInfo getClusterInfoFromObjectFullName(String objectFullName);

    public String getClusterNameFromBucketFullName(String bucketFullName);

    public void destroy();
}
