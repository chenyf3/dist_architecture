package com.xpay.web.api.common.ddo.vo;

public class ImageVo {
    /**
     * 文件名
     */
    private String name;
    /**
     * 过期时间
     */
    private Long expire;
    /**
     * 版本号，可选
     */
    private String version;

    public ImageVo(String name, Long expire){
        this.name = name;
        this.expire = expire;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
