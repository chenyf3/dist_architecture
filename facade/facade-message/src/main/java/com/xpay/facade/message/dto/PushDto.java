package com.xpay.facade.message.dto;

import java.io.Serializable;
import java.util.List;

public class PushDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer platform;//推送平台，详见：PushPlatformEnum
    private Integer appName;//应用名称，用以决定发送推送请求时要用哪个应用的appId、密钥等，详见：AppNameEnum
    private Integer osType;//终端操作系统类型，安卓、ios、鸿蒙 等，详见：OSTypeEnum
    private Integer target;//目标设备类型 详见：PushTargetEnum
    private List<String> targetValues;//目标设备的值列表，该字段使用哪种内容取决于 platform 和 pushTarget 两个字段的不同选择
    private String title;//标题
    private String jsonBody;//推送数据
    private String trxNo;//业务订单号，用以日志打印跟踪

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public Integer getAppName() {
        return appName;
    }

    public void setAppName(Integer appName) {
        this.appName = appName;
    }

    public Integer getOsType() {
        return osType;
    }

    public void setOsType(Integer osType) {
        this.osType = osType;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public List<String> getTargetValues() {
        return targetValues;
    }

    public void setTargetValues(List<String> targetValues) {
        this.targetValues = targetValues;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJsonBody() {
        return jsonBody;
    }

    public void setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
    }

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }
}
