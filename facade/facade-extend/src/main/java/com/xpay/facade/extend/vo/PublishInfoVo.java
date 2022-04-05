package com.xpay.facade.extend.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 项目发布时需要的相关信息
 */
public class PublishInfoVo implements Serializable {
    private String emailReceiver;
    private List<IdcVo> idcList;

    public String getEmailReceiver() {
        return emailReceiver;
    }

    public void setEmailReceiver(String emailReceiver) {
        this.emailReceiver = emailReceiver;
    }

    public List<IdcVo> getIdcList() {
        return idcList;
    }

    public void setIdcList(List<IdcVo> idcList) {
        this.idcList = idcList;
    }
}
