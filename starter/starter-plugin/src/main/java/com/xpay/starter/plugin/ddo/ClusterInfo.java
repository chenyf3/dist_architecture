package com.xpay.starter.plugin.ddo;

public class ClusterInfo {
    private String clusterName;//集群名称
    private String group;//业务组
    private String bucketName;//bucket名称

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
