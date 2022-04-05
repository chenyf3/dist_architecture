package com.xpay.starter.plugin.ddo;

import java.util.Date;

public class BucketInfo {
    private String name;//桶名称
    private Date createTime;//创建时间
    private String location;
    private String region;
    private String storageClass;
    private String redundancyType;//数据容灾类型
    private String ownerName;//拥有者的名称
    private String grant;//授权类型 public、private

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getRedundancyType() {
        return redundancyType;
    }

    public void setRedundancyType(String redundancyType) {
        this.redundancyType = redundancyType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getGrant() {
        return grant;
    }

    public void setGrant(String grant) {
        this.grant = grant;
    }
}
