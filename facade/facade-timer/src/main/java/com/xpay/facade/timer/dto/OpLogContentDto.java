package com.xpay.facade.timer.dto;

import java.io.Serializable;

public class OpLogContentDto implements Serializable {
    private Integer opType;
    private String logInfo;

    public Integer getOpType() {
        return opType;
    }

    public void setOpType(Integer opType) {
        this.opType = opType;
    }

    public String getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(String logInfo) {
        this.logInfo = logInfo;
    }
}
