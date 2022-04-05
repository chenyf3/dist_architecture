package com.xpay.common.api.dto;

import com.xpay.common.api.enums.VersionEnum;
import com.xpay.common.statics.dto.mq.MsgDto;

public class CallbackDto extends MsgDto {
    /**
     * 版本号
     */
    private String version = VersionEnum.VERSION_1_0.getValue();
    /**
     * 签名类型
     */
    private String signType;
    /**
     * 报文字段加密密钥
     */
    private String secKey;
    /**
     * 回调的数据
     */
    private Object data;
    /**
     * 商户流水号
     */
    private String mchTrxNo;
    /**
     * 回调地址
     */
    private String callbackUrl;
    /**
     * 最大回调次数
     */
    private int maxTimes = 5;
    /**
     * 当前回调次数（消息消费端设置）
     */
    private int currTimes = 1;
    /**
     * 基础延时间隔（毫秒）， 后续将以2的N-1次方乘以此数字作为重试的间隔时间，即第 1、2、3、4、5 次的回调间隔为：2000、4000、8000、16000、32000
     */
    private int baseDelay = 2000;
    /**
     * 发起通知的时间
     */
    private String notifyTime;
    /**
     * 业务线
     */
    private int productType;
    /**
     * 产品编码
     */
    private int productCode;

    private Long notifyRecordId;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMchTrxNo() {
        return mchTrxNo;
    }

    public void setMchTrxNo(String mchTrxNo) {
        this.mchTrxNo = mchTrxNo;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    public void setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
    }

    public int getCurrTimes() {
        return currTimes;
    }

    public void setCurrTimes(int currTimes) {
        this.currTimes = currTimes;
    }

    public int getBaseDelay() {
        return baseDelay;
    }

    public void setBaseDelay(int baseDelay) {
        this.baseDelay = baseDelay;
    }

    public String getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(String notifyTime) {
        this.notifyTime = notifyTime;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public int getProductCode() {
        return productCode;
    }

    public void setProductCode(int productCode) {
        this.productCode = productCode;
    }

    public Long getNotifyRecordId() {
        return notifyRecordId;
    }

    public void setNotifyRecordId(Long notifyRecordId) {
        this.notifyRecordId = notifyRecordId;
    }
}
