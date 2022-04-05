package com.xpay.starter.plugin.ddo;

import java.util.Date;
import java.util.Map;

public class ObjectInfo {
    private String bucket;
    private String object;
    private String region;
    private String contentType;
    private long length;//文件大小，单位：字节
    private String etag;//该对象的md5值
    public String versionId;
    private String suffix;//文件后缀
    private Date lastModified;
    private Map<String, String> userMetadata;

    private String storageClass;//阿里云OSS专属

    private String retentionMode;//minio专属
    private Date retentionRetainUntilDate;//minio专属


    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Map<String, String> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, String> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getRetentionMode() {
        return retentionMode;
    }

    public void setRetentionMode(String retentionMode) {
        this.retentionMode = retentionMode;
    }

    public Date getRetentionRetainUntilDate() {
        return retentionRetainUntilDate;
    }

    public void setRetentionRetainUntilDate(Date retentionRetainUntilDate) {
        this.retentionRetainUntilDate = retentionRetainUntilDate;
    }
}
