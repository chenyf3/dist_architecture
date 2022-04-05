package com.xpay.facade.extend.enums;

public enum BuildResultEnum {
    QUEUEING("QUEUEING", "排队中"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "成功"),
    FAILURE("FAILURE", "失败"),
    UNSTABLE("UNSTABLE", "不稳定"),
    ABORTED("ABORTED", "已取消");

    private String value;
    private String desc;

    private BuildResultEnum(String value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static BuildResultEnum getEnum(String value){
        BuildResultEnum[] enumAry = BuildResultEnum.values();
        for (int i = 0; i < enumAry.length; i++) {
            if (enumAry[i].getValue().equals(value)) {
                return enumAry[i];
            }
        }
        return null;
    }
}
