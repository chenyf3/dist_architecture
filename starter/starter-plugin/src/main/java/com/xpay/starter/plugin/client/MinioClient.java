package com.xpay.starter.plugin.client;

import com.xpay.starter.plugin.ddo.BucketInfo;
import com.xpay.starter.plugin.ddo.ClusterInfo;
import com.xpay.starter.plugin.ddo.ObjectInfo;
import com.xpay.starter.plugin.properties.MinioProperties;
import com.xpay.starter.plugin.util.MimeUtil;
import com.xpay.starter.plugin.util.HexUtil;
import com.xpay.starter.plugin.util.Utils;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * minio客户端，使用此客户端的bucket和object有如下命名规范：
 * bucket全名的格式为：集群名 + 业务团队名称 + bucket名，例如：cluster01-team1-test-bucket
 * object名的格式为：bucket全名 + 数据md5值 + 文件后缀，例如：cluster01-team1-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
 * 本客户端不提供对象的修改，因为对象内容变更会导致其md5值变更，那就无法做到和原对象的名称相同，要想达到修改的效果，可以先删除旧对象然后再重新上传新对象
 */
public class MinioClient implements ObjectStorage {
    public final static String CLUSTER_HEADER = "cluster-name";
    private final static String BUCKET_NOT_EXIST_CODE = "NoSuchBucket";
    private final static String OBJECT_NOT_EXIST_CODE = "NoSuchKey";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private io.minio.MinioClient minioClient;
    private final MinioProperties properties;

    public MinioClient(MinioProperties properties) {
        validProperties(properties);
        this.properties = properties;
        this.minioClient = io.minio.MinioClient.builder()
                .endpoint(properties.getEndpoint(), properties.getPort(), properties.isSecure())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        this.minioClient.setTimeout(properties.getConnectTimeout().toMillis(), properties.getWriteTimeout().toMillis(),
                properties.getReadTimeout().toMillis());
    }

    /**
     * 创建 bucket
     * @param bucketFullName
     * @return  创建成功则返回bucket全名，创建失败则返回null
     */
    @Override
    public String makeBucket(String bucketFullName) {
        try {
            bucketFullName = getBucketFullName(bucketFullName);
            boolean flag = bucketExists(bucketFullName);
            if (flag) {
                logger.info("bucketFullName:{} 已存在，无法创建", bucketFullName);
                return null;
            }

            Map<String, String> headers = getClusterHttpHeader(properties.getWriteCluster(), null);
            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketFullName)
                    .extraHeaders(headers)
                    .build();
            minioClient.makeBucket(args);
            return bucketFullName;
        } catch (Exception e) {
            logger.error("bucketFullName:{} 创建失败", bucketFullName, e);
            return null;
        }
    }

    /**
     * 判断bucket是否存在
     * @param bucketFullName    bucket全名
     * @return
     */
    @Override
    public boolean bucketExists(String bucketFullName) {
        try {
            BucketExistsArgs args = BucketExistsArgs.builder().bucket(bucketFullName).build();
            return minioClient.bucketExists(args);
        } catch (Exception e) {
            throw new RuntimeException("判断bucket是否存在时异常 bucketFullName:"+bucketFullName, e);
        }
    }

    @Override
    public BucketInfo getBucketInfo(String bucketName) {
        return null;
    }

    /**
     * 列出Server中的所有bucket名称(不限当前group)
     * @return
     */
    @Override
    public List<String> listBucketNames() {
        List<BucketInfo> bucketInfos = listBuckets();
        List<String> bucketNameList = new ArrayList<>();
        if (bucketInfos != null) {
            bucketInfos.forEach(bucket -> bucketNameList.add(bucket.getName()));
        }
        return bucketNameList;
    }

    /**
     * 列出Server中的所有bucket(不限当前group)
     * @return
     */
    @Override
    public List<BucketInfo> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<BucketInfo> infoList = new ArrayList<>();
            if (buckets != null) {
                buckets.forEach(bucket -> {
                    BucketInfo info = new BucketInfo();
                    info.setName(bucket.name());
                    info.setCreateTime(Utils.shanghaiTimeZone(bucket.creationDate()));
                    infoList.add(info);
                });
            }
            return infoList;
        } catch (Exception e) {
            logger.error("listBuckets异常", e);
            return null;
        }
    }

    /**
     * 删除一个bucket
     * @param bucketFullName    bucket全名
     * @return
     */
    @Override
    public boolean removeBucket(String bucketFullName) {
        boolean flag = bucketExists(bucketFullName);
        if (!flag) {
            return true;
        }

        boolean hasObject = hasObject(bucketFullName);
        if (hasObject) {
            logger.error("当前bucket中还存在object，不能删除 bucketFullName:{}", bucketFullName);
            return false;
        }

        //  Delete buckets , Be careful , Only when the bucket is empty can it be deleted successfully .
        try {
            String clusterName = getClusterNameFromBucketFullName(bucketFullName);
            Map<String, String> headers = getClusterHttpHeader(clusterName, null);
            RemoveBucketArgs args = RemoveBucketArgs.builder().bucket(bucketFullName).extraHeaders(headers).build();
            minioClient.removeBucket(args);
        } catch (Exception e) {
            logger.error("删除bucket异常 bucketFullName:{}", bucketFullName, e);
            return false;
        }
        flag = bucketExists(bucketFullName);
        return !flag;
    }

    /**
     * 判断一个bucket下是否存在有对象
     * @param bucketFullName    bucket全名
     * @return
     */
    @Override
    public boolean hasObject(String bucketFullName) {
        List<ObjectInfo> objectInfoList = listObjectInfos(bucketFullName, null, 1);
        return objectInfoList != null && objectInfoList.size() > 0;
    }

    /**
     * 列出bucket下的所有对象名(不能超过10万个)(分页查询)
     * @param bucketFullName
     * @return
     */
    @Override
    public List<String> listObjectNames(String bucketFullName) {
        List<String> objectNames = new ArrayList<>();
        boolean isContinue = true;
        String startAfter = null;
        int pageSize = 500;
        int maxSizeAllow = 100000;
        while (isContinue) {
            List<String> nameList = listObjectNames(bucketFullName, startAfter, pageSize);
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
     * @param bucketFullName    bucket全名
     * @param startAfter        起始对象名，如果从第一个开始，则传入null
     * @param limit             分页数量，最大不超过1000
     * @return
     */
    @Override
    public List<String> listObjectNames(String bucketFullName, String startAfter, int limit) {
        List<ObjectInfo> objectInfoList = listObjectInfos(bucketFullName, startAfter, limit);
        List<String> listObjectNames = new ArrayList<>();
        if(objectInfoList != null){
            objectInfoList.forEach(objInfo -> listObjectNames.add(objInfo.getObject()));
        }
        return listObjectNames;
    }

    /**
     * 分页查询bucket下对象信息
     * @param bucketFullName    bucket名称
     * @param startAfter        起始对象名，如果从第一个开始，则传入null
     * @param limit             分页数量，最大不超过1000
     * @return
     */
    @Override
    public List<ObjectInfo> listObjectInfos(String bucketFullName, String startAfter, int limit) {
        boolean flag = bucketExists(bucketFullName);
        if (!flag) {
            return new ArrayList<>();
        }

        String clusterName = getClusterNameFromBucketFullName(bucketFullName);
        Map<String, String> headers = getClusterHttpHeader(clusterName, null);
        ListObjectsArgs.Builder builder = ListObjectsArgs.builder().bucket(bucketFullName).extraHeaders(headers);
        if (startAfter != null) {
            builder.startAfter(startAfter);
        }
        ListObjectsArgs args = builder.maxKeys(limit).build();
        Iterable<Result<Item>> iterable = minioClient.listObjects(args);
        Iterator<Result<Item>> iterator = iterable != null ? iterable.iterator() : null;
        List<ObjectInfo> infoList = new ArrayList<>();
        while (iterator != null && iterator.hasNext()) {
            Result<Item> result = iterator.next();
            Item item;
            try {
                item = result.get();
            } catch (Exception e) {
                throw new RuntimeException("获取Item信息时异常", e);
            }
            ObjectInfo objInfo = new ObjectInfo();
            objInfo.setBucket(bucketFullName);
            objInfo.setObject(item.objectName());
            objInfo.setRegion(null);
            objInfo.setContentType(null);
            objInfo.setLength(item.size());
            objInfo.setEtag(item.etag());
            objInfo.setLastModified(Utils.shanghaiTimeZone(item.lastModified()));
            objInfo.setUserMetadata(item.userMetadata());
            objInfo.setVersionId(item.versionId());
            infoList.add(objInfo);
        }
        return infoList;
    }

    /**
     * 上传对象，会自动根据文件头信息判断当前文件的contentType，但是支持的类型比较有限
     * @param bucketFullName    bucket全名
     * @param oriFileName       原始文件名
     * @param data              要上传的数据
     * @return 上传成功返回在服务端存储的对象名，上传失败则返回null
     */
    @Override
    public String putObject(String bucketFullName, String oriFileName, byte[] data) {
        String contentType = MimeUtil.getMimeType(data);
        return putObject(bucketFullName, oriFileName, data, contentType, null, null);
    }

    /**
     * 上传对象
     * @param bucketFullName    bucket名称
     * @param oriFileName       原始文件名
     * @param data              要上传的数据
     * @param contentType       contentType
     * @return 上传成功返回在服务端存储的对象名，上传失败则返回null
     */
    @Override
    public String putObject(String bucketFullName, String oriFileName, byte[] data, String contentType) {
        bucketFullName = getBucketFullName(bucketFullName);
        String objectFullName = getObjectFullName(bucketFullName, data, oriFileName);
        return putObject(bucketFullName, objectFullName, data, contentType, null, null);
    }

    /**
     * 上传对象
     * @param bucketFullName    bucket全名
     * @param objectFullName    object全名
     * @param data              要上传的数据
     * @param contentType       contentType
     * @param headers           http请求头信息
     * @param metaData          自定义的元数据
     * @return 上传成功返回在服务端存储的对象名，上传失败则返回null
     */
    @Override
    public String putObject(String bucketFullName, String objectFullName, byte[] data, String contentType,
                            Map<String, String> headers, Map<String, String> metaData) {
        try {
            bucketFullName = getBucketFullName(bucketFullName);//如果没有填充全名，则强制填充
            objectFullName = getObjectFullName(bucketFullName, objectFullName);//如果没有填充全名，则强制填充
            String clusterName = getClusterNameFromBucketFullName(bucketFullName);
            headers = getClusterHttpHeader(clusterName, headers);

            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            PutObjectArgs args = PutObjectArgs.builder()
                    .headers(headers)
                    .bucket(bucketFullName)
                    .object(objectFullName)
                    .contentType(contentType)
                    .stream(stream, data.length, properties.getFileSize())
                    .userMetadata(metaData)
                    .build();

            ObjectWriteResponse response = null;
            try {
                response = minioClient.putObject(args);
            } catch (Exception e) {
                if (isBucketNotExistException(e)) { //如果bucket不存在，则先创建
                    String createdName = makeBucket(bucketFullName);
                    if (createdName != null) {
                        response = minioClient.putObject(args);
                    }
                } else {
                    throw e;
                }
            }
            boolean isSuccess = response != null && Utils.isNotEmpty(response.etag());
            return isSuccess ? objectFullName : null;
        } catch (Exception e) {
            logger.error("文件上传失败 bucketFullName:{} objectFullName:{}", bucketFullName, objectFullName, e);
            return null;
        }
    }

    /**
     * 获取对象
     * @param objectFullName  object全名
     * @return
     */
    @Override
    public byte[] getObject(String objectFullName) {
        ClusterInfo info = getClusterInfoFromObjectFullName(objectFullName);
        String clusterName = info.getClusterName();
        String bucketFullName = info.getBucketName();

        Map<String, String> headers = getClusterHttpHeader(clusterName, null);
        InputStream stream;
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .extraHeaders(headers)
                    .bucket(bucketFullName)
                    .object(objectFullName)
                    .build();
            stream = minioClient.getObject(args);
            if(stream == null){
                return null;
            }
        } catch (Exception e) {
            if (isBucketNotExistException(e) || isObjectNotExistException(e)) {
                return null;
            } else {
                throw new RuntimeException("获取object异常,bucketFullName="+bucketFullName+",objectFullName="+objectFullName, e);
            }
        }

        try {
            return Utils.readBytes(stream);
        } catch (Exception e) {
            throw new RuntimeException("读取Stream异常,bucketFullName="+bucketFullName+",objectFullName="+objectFullName, e);
        } finally {
            try {
                stream.close();
            } catch (Exception e){
            }
        }
    }

    /**
     * 分段获取对象
     * @param objectFullName 包含了bucket名称和cluster名的对象名
     * @param offset         起始位置
     * @param length         获取的长度
     * @return
     */
    @Override
    public byte[] getObject(String objectFullName, long offset, long length) {
        ClusterInfo info = getClusterInfoFromObjectFullName(objectFullName);
        String clusterName = info.getClusterName();
        String bucketFullName = info.getBucketName();

        Map<String, String> headers = getClusterHttpHeader(clusterName, null);
        GetObjectArgs args = GetObjectArgs.builder()
                .extraHeaders(headers)
                .bucket(bucketFullName)
                .object(objectFullName)
                .offset(offset)
                .length(length)
                .build();

        InputStream stream;
        try {
            stream = minioClient.getObject(args);
            if(stream == null){
                return null;
            }
        } catch (Exception e) {
            if (isBucketNotExistException(e) || isObjectNotExistException(e)) {
                return null;
            } else {
                throw new RuntimeException("获取object异常,bucketFullName="+bucketFullName+",objectFullName="+objectFullName, e);
            }
        }

        try {
            return Utils.readBytes(stream);
        } catch (Exception e) {
            throw new RuntimeException("读取Stream异常,bucketFullName="+bucketFullName+",objectFullName="+objectFullName, e);
        } finally {
            try {
                stream.close();
            } catch (Exception e){
            }
        }
    }

    /**
     * 获取对象相关信息
     * @param objectFullName     包含了bucket名称和cluster名的对象名
     * @return  返回对象相关信息
     */
    @Override
    public ObjectInfo getObjectInfo(String objectFullName, String version) {
        ClusterInfo info = getClusterInfoFromObjectFullName(objectFullName);
        String clusterName = info.getClusterName();
        String bucketFullName = info.getBucketName();

        try {
            Map<String, String> headers = getClusterHttpHeader(clusterName, null);
            StatObjectArgs args = StatObjectArgs.builder()
                    .extraHeaders(headers)
                    .bucket(bucketFullName)
                    .object(objectFullName)
                    .build();
            StatObjectResponse response = minioClient.statObject(args);
            ObjectInfo objInfo = new ObjectInfo();
            objInfo.setBucket(response.bucket());
            objInfo.setObject(response.object());
            objInfo.setSuffix(getFileNameSuffix(objectFullName));
            objInfo.setRegion(response.region());
            objInfo.setContentType(response.contentType());
            objInfo.setLength(response.size());
            objInfo.setEtag(response.etag());
            objInfo.setLastModified(Utils.shanghaiTimeZone(response.lastModified()));
            objInfo.setUserMetadata(response.userMetadata());
            objInfo.setVersionId(response.versionId());
            objInfo.setRetentionMode(response.retentionMode() != null ? response.retentionMode().name() : null);
            objInfo.setRetentionRetainUntilDate(Utils.shanghaiTimeZone(response.retentionRetainUntilDate()));
            return objInfo;
        } catch (Exception e) {
            if (isBucketNotExistException(e) || isObjectNotExistException(e)) {
                return null;
            } else {
                throw new RuntimeException("查询对象信息异常,bucketFullName="+bucketFullName+",objectFullName="+objectFullName, e);
            }
        }
    }

    /**
     * 删除对象
     * @param objectFullName 包含了bucket名称和cluster名的对象名
     * @return  删除成功返回true，删除失败返回false
     */
    @Override
    public boolean removeObject(String objectFullName) {
        ClusterInfo info = getClusterInfoFromObjectFullName(objectFullName);
        String clusterName = info.getClusterName();
        String bucketFullName = info.getBucketName();

        try {
            Map<String, String> headers = getClusterHttpHeader(clusterName, null);
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .extraHeaders(headers)
                    .bucket(bucketFullName)
                    .object(objectFullName)
                    .build();
            minioClient.removeObject(args);
            return true;
        } catch (Exception e) {
            if (isBucketNotExistException(e) || isObjectNotExistException(e)) {
                return true;
            } else {
                logger.error("删除失败 bucketFullName:{} objectFullName:{}", bucketFullName, objectFullName, e);
                return false;
            }
        }
    }

    /**
     * 批量删除对象
     * @param objectNames       对象名列表
     * @return  删除成功返回true，删除失败返回false
     */
    @Override
    public boolean removeObject(List<String> objectNames) {
        ClusterInfo info = getClusterInfoFromObjectFullName(objectNames.get(0));
        String bucketFullName = info.getBucketName();
        String clusterName = info.getClusterName();
        for (String objectFullName : objectNames) {
            if (! objectFullName.startsWith(bucketFullName)) {
                throw new IllegalArgumentException("只能批量删除同一个bucket下的对象");
            }
        }

        boolean flag = bucketExists(bucketFullName);
        if(!flag){
            return true;
        }

        Map<String, String> headers = getClusterHttpHeader(clusterName, null);
        List<DeleteObject> objects = new LinkedList<>();
        objectNames.forEach(name -> objects.add(new DeleteObject(name)));

        RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .extraHeaders(headers)
                .bucket(bucketFullName)
                .objects(objects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                logger.error("object删除失败 objectName:{} errMessage:{}", error.objectName(), error.message());
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成对象的访问链接
     * @param objectFullName     包含了bucket名称和cluster名的对象名
     * @param expireSec          有效时间(秒)
     * @return  返回访问地址url
     */
    @Override
    public String getObjectURL(String objectFullName, Integer expireSec) {
        ClusterInfo info = getClusterInfoFromObjectFullName(objectFullName);
        String clusterName = info.getClusterName();
        String bucketFullName = info.getBucketName();

        Map<String, String> headers = getClusterHttpHeader(clusterName, null);
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .extraHeaders(headers)
                .method(Method.GET)
                .bucket(bucketFullName)
                .object(objectFullName)
                .expiry(expireSec, TimeUnit.SECONDS)
                .build();
        try {
            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException("获取文件链接时异常", e);
        }
    }

    /**
     * 填充bucket全名，为bucket名称拼接上 集群名 + 业务团队名 前缀，例如：cluster01-team1-
     * @param bucketName
     * @return
     */
    @Override
    public String getBucketFullName(String bucketName) {
        //实时构建前缀，为了能够实现配置的热更新
        StringBuilder sb = new StringBuilder().append(properties.getWriteCluster())
                .append(BUCKET_SEPARATOR)
                .append(properties.getGroup())
                .append(BUCKET_SEPARATOR);//如：cluster01-team1
        if (bucketName.startsWith(sb.toString())) {
            return bucketName;
        }
        return sb.toString() + bucketName;
    }

    /**
     * 填充object全名（适用于需要自定义对象名的情况），object全名的格式为：bucket全名 + 自定义文件名
     * @param bucketFullName    bucket全名
     * @param objectName        原对象名
     * @return  返回包含集群信息的对象名，例如：cluster01-team1-test-bucket_selfDefineFileName.png
     */
    @Override
    public String getObjectFullName(String bucketFullName, String objectName) {
        if(objectName.startsWith(bucketFullName)){
            return objectName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bucketFullName)
                .append(OBJECT_SEPARATOR)
                .append(objectName);
        return sb.toString();
    }

    /**
     * 填充object全名，object全名的格式为：bucket全名 + 上传对象的md5值 + 文件后缀，例如：cluster01-team1-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
     * @param bucketFullName   集群全名
     * @param data             字节数据
     * @param oriFileName      原文件名(用以取得文件后缀)
     * @return  返回包含集群信息的对象名，例如：cluster01-team1-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
     */
    @Override
    public String getObjectFullName(String bucketFullName, byte[] data, String oriFileName) {
        String fileName = HexUtil.encodeHexMd5(data);//获取当前数据的md5值
        String suffix = getFileNameSuffix(oriFileName);//取得文件后缀
        StringBuilder sb = new StringBuilder();
        sb.append(bucketFullName)
                .append(OBJECT_SEPARATOR)
                .append(fileName)
                .append(suffix);
        return sb.toString();
    }

    /**
     * 从objectName中分离出clusterName、bucketName
     * @see #getObjectFullName(String, byte[], String)
     * @param objectFullName 包含集群信息的对象名，例如：cluster01-team1-test-bucket_b4dd93127a45c1bd80a13b490b517234.png
     * @return
     */
    @Override
    public ClusterInfo getClusterInfoFromObjectFullName(String objectFullName) {
        int clusterStartIndex = objectFullName.indexOf(BUCKET_SEPARATOR);//第一个分隔符
        int bucketStartIndex = objectFullName.indexOf(OBJECT_SEPARATOR);
        int groupStartIndex = clusterStartIndex + 1;
        int groupEndIndex = objectFullName.indexOf(BUCKET_SEPARATOR, clusterStartIndex+1);

        ClusterInfo info = new ClusterInfo();
        info.setClusterName(objectFullName.substring(0, clusterStartIndex));
        info.setBucketName(objectFullName.substring(0, bucketStartIndex));
        info.setGroup(objectFullName.substring(groupStartIndex, groupEndIndex));
        return info;
    }

    /**
     * 从bucket全名中分离出集群名
     * @param bucketFullName
     * @return
     */
    @Override
    public String getClusterNameFromBucketFullName(String bucketFullName){
        int clusterIndex = bucketFullName.indexOf(BUCKET_SEPARATOR);
        return bucketFullName.substring(0, clusterIndex);
    }

    /**
     * 销毁客户端对象
     */
    @Override
    public void destroy() {
        if (minioClient != null) {
            minioClient = null;
        }
    }

    /**
     * 判断是否是bucket不存的异常
     * @param e
     * @return
     */
    private boolean isBucketNotExistException(Exception e) {
        return e instanceof ErrorResponseException
                && BUCKET_NOT_EXIST_CODE.equals(((ErrorResponseException) e).errorResponse().code());
    }

    /**
     * 判断是否是对象不存在的异常
     * @param e
     * @return
     */
    private boolean isObjectNotExistException(Exception e) {
        return e instanceof ErrorResponseException
                && OBJECT_NOT_EXIST_CODE.equals(((ErrorResponseException) e).errorResponse().code());
    }

    /**
     * 往http请求头中添加集群信息
     * @param clusterName
     * @param headers
     * @return
     */
    private Map<String, String> getClusterHttpHeader(String clusterName, Map<String, String> headers) {
        if (headers == null) {
            return Collections.singletonMap(CLUSTER_HEADER, clusterName);
        } else {
            headers.put(CLUSTER_HEADER, clusterName);
            return headers;
        }
    }

    /**
     * 参数校验
     * @param properties
     */
    private void validProperties(MinioProperties properties) {
        if (Utils.isEmpty(properties.getEndpoint())) {
            throw new IllegalArgumentException("请配置endpoint地址");
        } else if(Utils.isEmpty(properties.getGroup())) {
            throw new IllegalArgumentException("请配置group业务团队名");
        }  else if(Utils.isEmpty(properties.getAccessKey())) {
            throw new IllegalArgumentException("请配置accessKey");
        } else if(Utils.isEmpty(properties.getSecretKey())) {
            throw new IllegalArgumentException("请配置secretKey");
        }
    }
}
