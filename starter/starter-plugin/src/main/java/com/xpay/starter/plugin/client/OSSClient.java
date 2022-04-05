package com.xpay.starter.plugin.client;

import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import com.xpay.starter.plugin.ddo.ClusterInfo;
import com.xpay.starter.plugin.ddo.ObjectInfo;
import com.xpay.starter.plugin.properties.OSSProperties;
import com.xpay.starter.plugin.util.HexUtil;
import com.xpay.starter.plugin.util.MimeUtil;
import com.xpay.starter.plugin.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * 阿里云OSS对象存储客户端，使用此客户端的bucket和object有如下命名规范：
 * bucket名称格式为：只能字母、数字、中划线组成，例如：my-test-bucket
 * object名的格式为：bucket名 + 数据md5值 + 文件后缀，例如：my-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
 * 本客户端不提供对象的修改，因为对象内容变更会导致其md5值变更，那就无法做到和原对象的名称相同，要想达到修改的效果，可以先删除旧对象然后再重新上传新对象
 */
public class OSSClient implements ObjectStorage {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OSS ossClient;

    public OSSClient(OSSProperties properties){
        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setMaxConnections(properties.getMaxConnections());
        configuration.setSocketTimeout(properties.getSocketTimeout());
        configuration.setConnectionTimeout(properties.getConnectionTimeout());
        configuration.setConnectionRequestTimeout(properties.getConnectionRequestTimeout());
        configuration.setIdleConnectionTime(properties.getIdleConnectionTime());
        String endpoint = properties.getHost();
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey, configuration);
    }

    /**
     * 创建 bucket
     * @param bucketName    bucket名
     * @return  创建成功则返回bucket名，创建失败则返回null
     */
    @Override
    public String makeBucket(String bucketName){
        checkBucketNotNull(bucketName);
        boolean flag = bucketExists(bucketName);
        if (flag) {
            logger.info("bucketName:{} 已存在，无法创建", bucketName);
            return null;
        }

        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        // 如果创建存储空间的同时需要指定存储类型、存储空间的读写权限、数据容灾类型, 请参考如下代码。
        // 此处以设置存储空间的存储类型为标准存储为例介绍。
        createBucketRequest.setStorageClass(StorageClass.Standard);
        // 数据容灾类型默认为本地冗余存储，即DataRedundancyType.LRS。
        createBucketRequest.setDataRedundancyType(DataRedundancyType.LRS);
        // 设置存储空间读写权限为私有。
        createBucketRequest.setCannedACL(CannedAccessControlList.Private);
        Bucket bucket = ossClient.createBucket(createBucketRequest);
        return bucket == null ? null : bucket.getName();
    }

    /**
     * 判断bucket是否存在
     * @param bucketName    bucket名
     * @return
     */
    @Override
    public boolean bucketExists(String bucketName) {
        checkBucketNotNull(bucketName);
        try {
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            throw new RuntimeException("判断bucket是否存在时异常 bucketName:"+bucketName, e);
        }
    }

    /**
     * 获取bucket相关信息
     * @param bucketName    bucket名
     * @return
     */
    @Override
    public com.xpay.starter.plugin.ddo.BucketInfo getBucketInfo(String bucketName){
        checkBucketNotNull(bucketName);
        com.aliyun.oss.model.BucketInfo info = ossClient.getBucketInfo(bucketName);

        com.xpay.starter.plugin.ddo.BucketInfo bucketInfo = new com.xpay.starter.plugin.ddo.BucketInfo();
        bucketInfo.setName(info.getBucket().getName());
        bucketInfo.setCreateTime(info.getBucket().getCreationDate());
        bucketInfo.setLocation(info.getBucket().getLocation());
        bucketInfo.setRegion(info.getBucket().getRegion());
        bucketInfo.setOwnerName(info.getBucket().getOwner().getDisplayName());
        bucketInfo.setStorageClass(info.getBucket().getStorageClass().name());
        bucketInfo.setRedundancyType(info.getDataRedundancyType().name());
        bucketInfo.setGrant(info.getCannedACL().name());
        return bucketInfo;
    }

    /**
     * 列出所有bucket名称
     * @return
     */
    @Override
    public List<String> listBucketNames() {
        List<com.xpay.starter.plugin.ddo.BucketInfo> bucketInfos = listBuckets();
        List<String> bucketNameList = new ArrayList<>();
        if (bucketInfos != null) {
            bucketInfos.forEach(bucket -> bucketNameList.add(bucket.getName()));
        }
        return bucketNameList;
    }

    /**
     * 列出所有bucket
     * @return
     */
    @Override
    public List<com.xpay.starter.plugin.ddo.BucketInfo> listBuckets() {
        List<Bucket> buckets = ossClient.listBuckets();
        if (buckets == null || buckets.isEmpty()) {
            return new ArrayList<>();
        }

        List<com.xpay.starter.plugin.ddo.BucketInfo> bucketInfoList = new ArrayList<>();
        buckets.forEach(bucket -> {
            com.xpay.starter.plugin.ddo.BucketInfo info = new com.xpay.starter.plugin.ddo.BucketInfo();
            info.setName(bucket.getName());
            info.setCreateTime(bucket.getCreationDate());
            info.setLocation(bucket.getLocation());
            info.setRegion(bucket.getRegion());
            info.setOwnerName(bucket.getOwner().getDisplayName());
            info.setStorageClass(bucket.getStorageClass().name());
            bucketInfoList.add(info);
        });
        return bucketInfoList;
    }

    /**
     * 删除bucket
     * @param bucketName    bucket名
     * @return
     */
    @Override
    public boolean removeBucket(String bucketName) {
        checkBucketNotNull(bucketName);
        boolean flag = bucketExists(bucketName);
        if (!flag) {
            return true;
        }

        boolean hasObject = hasObject(bucketName);
        if (hasObject) {
            logger.error("当前bucket中还存在object，不能删除，bucketName: {}", bucketName);
            return false;
        }

        ossClient.deleteBucket(bucketName);
        flag = bucketExists(bucketName);
        return !flag;
    }

    /**
     * 判断一个bucket下是否存在有对象
     * @param bucketName    bucket名
     * @return
     */
    @Override
    public boolean hasObject(String bucketName) {
        checkBucketNotNull(bucketName);
        List<ObjectInfo> objectInfoList = listObjectInfos(bucketName, null, 1);
        return objectInfoList != null && objectInfoList.size() > 0;
    }

    /**
     * 列出bucket下的所有对象名(不能超过10万个)(分页查询)
     * @param bucketName    bucket名
     * @return
     */
    @Override
    public List<String> listObjectNames(String bucketName) {
        checkBucketNotNull(bucketName);
        List<String> objectNames = new ArrayList<>();
        boolean isContinue = true;
        String startAfter = null;
        int pageSize = 500;
        int maxSizeAllow = 100000;
        while (isContinue) {
            List<String> nameList = listObjectNames(bucketName, startAfter, pageSize);
            if (nameList != null && nameList.size() > 0) {
                objectNames.addAll(nameList);
                startAfter = nameList.get(nameList.size() - 1);
            }
            if (nameList == null || nameList.size() < pageSize) {
                isContinue = false;
            }
            if(objectNames.size() > maxSizeAllow) {
                throw new RuntimeException("当前bucket下的对象数量超过"+maxSizeAllow+"，无法获取");
            }
        }
        return objectNames;
    }

    /**
     * 分页列出bucket下对象名
     * @param bucketName    bucket名
     * @param startAfter    起始对象名，表示获取排在这个对象之后的对象，从第一个开始则传null
     * @param limit         分页数量，最大不超过1000
     * @return
     */
    @Override
    public List<String> listObjectNames(String bucketName, String startAfter, int limit) {
        checkBucketNotNull(bucketName);
        List<ObjectInfo> objectInfoList = listObjectInfos(bucketName, startAfter, limit);
        List<String> listObjectNames = new ArrayList<>();
        if(objectInfoList != null){
            objectInfoList.forEach(objInfo -> listObjectNames.add(objInfo.getObject()));
        }
        return listObjectNames;
    }

    /**
     * 列出bucket下的所有对象名(分页查询)
     * @param bucketName    bucket名
     * @param startAfter    起始对象名，表示获取排在这个对象之后的对象，从第一个开始则传null
     * @param limit         获取的对象数
     * @return
     */
    @Override
    public List<ObjectInfo> listObjectInfos(String bucketName, String startAfter, int limit) {
        checkBucketNotNull(bucketName);
        ListObjectsV2Request request = new ListObjectsV2Request(bucketName);
        request.setStartAfter(startAfter);
        request.setMaxKeys(limit);

        ListObjectsV2Result result = ossClient.listObjectsV2(request);
        List<OSSObjectSummary> objectSummaries = result.getObjectSummaries();

        List<ObjectInfo> objectInfoList = new ArrayList<>();
        objectSummaries.forEach(summary -> {
            ObjectInfo info = new ObjectInfo();
            info.setBucket(summary.getBucketName());
            info.setObject(summary.getKey());
            info.setLength(summary.getSize());
            info.setEtag(summary.getETag());
            info.setLastModified(summary.getLastModified());
            info.setStorageClass(summary.getStorageClass());
            objectInfoList.add(info);
        });
        return objectInfoList;
    }

    /**
     * 上传对象，会自动根据文件头信息判断当前文件的contentType，但是支持的类型比较有限
     * @param bucketName   bucket名
     * @param oriFileName  原始文件名
     * @param data         要上传的数据
     * @return 上传成功返回在服务端存储的对象名，上传失败则返回null
     */
    @Override
    public String putObject(String bucketName, String oriFileName, byte[] data) {
        String contentType = MimeUtil.getMimeType(data);
        return putObject(bucketName, oriFileName, data, contentType);
    }

    /**
     * 上传对象
     * @param bucketName    bucket名称
     * @param oriFileName   原始文件名
     * @param data          要上传的数据
     * @param contentType   contentType媒体类型
     * @return 上传成功返回在服务端存储的对象名，上传失败则返回null
     */
    @Override
    public String putObject(String bucketName, String oriFileName, byte[] data, String contentType) {
        String objectFullName = getObjectFullName(bucketName, data, oriFileName);
        return putObject(bucketName, objectFullName, data, contentType, null, null);
    }

    /**
     * 上传对象
     * @param bucketName        bucket名称
     * @param objectFullName    原始文件名
     * @param data              要上传的数据
     * @param contentType       contentType媒体类型
     * @param metaData          用户自定义的元数据
     * @return
     */
    @Override
    public String putObject(String bucketName, String objectFullName, byte[] data, String contentType,
                            Map<String, String> headers, Map<String, String> metaData){
        objectFullName = getObjectFullName(bucketName, objectFullName);//如果没有填充全名，则强制填充
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);
            if(metaData != null && metaData.size() > 0) {
                metaData.forEach((key,val) -> {
                    if (!key.startsWith("x-oss-meta-")) {
                        key = "x-oss-meta-" + key;
                    }
                    objectMetadata.addUserMetadata(key, val);
                });
            }

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectFullName, new ByteArrayInputStream(data), objectMetadata);
            // 上传字符串
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            if (result != null && result.getETag() != null) {
                return objectFullName;
            }
        } catch (OSSException oe) {
            logger.error("对象上传失败 ErrorCode: {}, ErrorMessage: {}, RequestId: {}, HostId: {}",
                    oe.getErrorCode(), oe.getErrorMessage(), oe.getRequestId(), oe.getHostId());
        } catch (ClientException ce) {
            logger.error("对象上传失败 ErrorCode: {}, ErrorMessage: {}, RequestId: {}",
                    ce.getErrorCode(), ce.getErrorMessage(), ce.getRequestId());
        }
        return null;
    }

    /**
     * 获取对象
     * @param objectFullName  包含了bucket名称的对象名
     * @return
     */
    @Override
    public byte[] getObject(String objectFullName){
        String bucketName = getBucketNameFormObjectFullName(objectFullName);
        OSSObject ossObject = null;
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, objectFullName);
            ossObject = ossClient.getObject(request);
            return Utils.readBytes(ossObject.getObjectContent());
        } catch (Throwable e) {
            throw new RuntimeException("读取Stream异常,objectFullName="+objectFullName, e);
        } finally {
            if (ossObject != null) {
                try {
                    ossObject.close();
                }catch (Exception e){
                }
            }
        }
    }

    /**
     * 分段获取对象
     * @param objectFullName 包含了bucket名称的对象名
     * @param offset         起始位置，第一个字节从0开始
     * @param length         获取的长度
     * @return
     */
    @Override
    public byte[] getObject(String objectFullName, long offset, long length) {
        String bucketName = getBucketNameFormObjectFullName(objectFullName);
        OSSObject ossObject = null;
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, objectFullName);
            request.setRange(offset, offset + length - 1);
            request.addHeader("x-oss-range-behavior", "standard");//当range范围超出文件大小时只返回剩余的数据，不会返回整个文件
            ossObject = ossClient.getObject(request);
            return Utils.readBytes(ossObject.getObjectContent());
        } catch (Throwable e) {
            throw new RuntimeException("读取Stream异常,objectFullName="+objectFullName, e);
        } finally {
            if (ossObject != null) {
                try {
                    ossObject.close();
                }catch (Exception e){
                }
            }
        }
    }

    /**
     * 获取对象信息
     * @param objectFullName    包含了bucket名称的对象名
     * @param version           对象版本号，如果没开启版本控制，则传null
     * @return
     */
    @Override
    public ObjectInfo getObjectInfo(String objectFullName, String version){
        String bucketName = getBucketNameFormObjectFullName(objectFullName);
        // 获取指定版本文件的全部元信息。
        HeadObjectRequest request = new HeadObjectRequest(bucketName, objectFullName, version);
        ObjectMetadata metadata = ossClient.headObject(request);

        ObjectInfo info = new ObjectInfo();
        info.setBucket(bucketName);
        info.setObject(objectFullName);
        info.setSuffix(getFileNameSuffix(objectFullName));
        info.setContentType(metadata.getContentType());
        info.setLength(metadata.getContentLength());
        info.setEtag(metadata.getETag());
        info.setVersionId(metadata.getVersionId());
        info.setStorageClass(metadata.getObjectStorageClass().name());
        info.setLastModified(metadata.getLastModified());
        info.setUserMetadata(metadata.getUserMetadata());
        return info;
    }

    /**
     * 删除对象
     * @param objectFullName 包含了bucket名称的对象名
     * @return  删除成功返回true，删除失败返回false
     */
    @Override
    public boolean removeObject(String objectFullName){
        try {
            String bucketName = getBucketNameFormObjectFullName(objectFullName);
            ossClient.deleteObject(bucketName, objectFullName);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("对象删除异常,objectFullName="+objectFullName, e);
        }
    }

    /**
     * 批量删除对象
     * @param objectNames       对象名列表
     * @return  删除成功返回true，删除失败返回false
     */
    @Override
    public boolean removeObject(List<String> objectNames) {
        String bucketName = getBucketNameFormObjectFullName(objectNames.get(0));
        for (String objectFullName : objectNames) {
            if (! objectFullName.startsWith(bucketName)) {
                throw new IllegalArgumentException("只能批量删除同一个bucket下的对象");
            }
        }

        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName).withKeys(objectNames);
            DeleteObjectsResult result = ossClient.deleteObjects(request);
            List<String> deletedObjects = result.getDeletedObjects();

            int deleteFailCount = 0;
            for (String objectFullName : objectNames) {
                if (!deletedObjects.contains(objectFullName)){
                    deleteFailCount ++;
                    logger.error("object删除失败 objectName:{}", objectFullName);
                }
            }
            return deleteFailCount == 0;
        } catch (Exception e) {
            throw new RuntimeException("批量对象删除异常", e);
        }
    }

    /**
     * 生成对象的访问链接
     * @param objectFullName     包含了bucket名称的对象名
     * @param expireSec          有效时间(秒)
     * @return  返回访问地址url
     */
    @Override
    public String getObjectURL(String objectFullName, Integer expireSec) {
        String bucketName = getBucketNameFormObjectFullName(objectFullName);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectFullName);
        // 设置签名URL过期时间。
        request.setExpiration(new Date(new Date().getTime() + expireSec * 1000));
        // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public String getBucketFullName(String bucketName) {
        return bucketName;
    }

    /**
     * 填充object全名（适用于需要自定义对象名的情况），object全名的格式为：bucket全名 + 自定义文件名
     * @param bucketName    bucket名
     * @param objectName    原对象名
     * @return  返回包含集群信息的对象名，例如：my-test-bucket_selfDefineFileName.png
     */
    @Override
    public String getObjectFullName(String bucketName, String objectName) {
        checkBucketNotNull(bucketName);
        if(objectName.startsWith(bucketName)){
            return objectName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bucketName)
                .append(OBJECT_SEPARATOR)
                .append(objectName);
        return sb.toString();
    }

    /**
     * 填充object全名，object全名的格式为：bucket名 + 上传对象的md5值 + 文件后缀，例如：my-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
     * @param bucketName     bucket名
     * @param data           字节数据
     * @param oriFileName    原文件名(用以取得文件后缀)
     * @return  返回包含集群信息的对象名，例如：my-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
     */
    @Override
    public String getObjectFullName(String bucketName, byte[] data, String oriFileName) {
        checkBucketNotNull(bucketName);
        String fileName = HexUtil.encodeHexMd5(data);//获取当前数据的md5值
        String suffix = getFileNameSuffix(oriFileName);//取得文件后缀
        StringBuilder sb = new StringBuilder();
        sb.append(bucketName)
                .append(OBJECT_SEPARATOR)
                .append(fileName)
                .append(suffix);
        return sb.toString();
    }

    @Override
    public ClusterInfo getClusterInfoFromObjectFullName(String objectFullName) {
        return null;
    }

    @Override
    public String getClusterNameFromBucketFullName(String bucketFullName) {
        return "";
    }

    /**
     * 销毁客户端对象
     */
    @Override
    public void destroy(){
        this.ossClient.shutdown();
    }

    private String getBucketNameFormObjectFullName(String objectFullName){
        if(Utils.isEmpty(objectFullName)) {
            throw new IllegalArgumentException("objectFullName不能为空");
        }
        int objectStartIndex = objectFullName.indexOf(OBJECT_SEPARATOR);
        return objectFullName.substring(0, objectStartIndex);
    }

    private static void checkBucketNotNull(String bucketName) {
        if(Utils.isEmpty(bucketName)) {
            throw new IllegalArgumentException("bucketName不能为空");
        }
    }
}
