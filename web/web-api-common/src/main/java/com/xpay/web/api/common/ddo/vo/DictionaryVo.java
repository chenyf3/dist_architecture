package com.xpay.web.api.common.ddo.vo;

import java.util.Date;
import java.util.List;

/**
 * 数据字典VO
 */
public class DictionaryVo {
    private int currentPage;
    private int pageSize;
    private Long id;
    private Integer version;
    private String dataName;
    private List<Item> dataInfo;
    private String remark;
    private Date createTime;
    private Date modifyTime;
    private String creator;
    private String modifyUser;
    /**
     * 系统标识 {@link com.xpay.common.statics.enums.user.SystemTypeEnum}
     */
    private Integer systemType;

    public DictionaryVo() {
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public List<Item> getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(List<Item> dataInfo) {
        this.dataInfo = dataInfo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public Integer getSystemType() {
        return systemType;
    }

    public void setSystemType(Integer systemType) {
        this.systemType = systemType;
    }

    public static class Item {
        private String name;
        private String code;
        private String desc;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public Item() {
        }
    }

}
