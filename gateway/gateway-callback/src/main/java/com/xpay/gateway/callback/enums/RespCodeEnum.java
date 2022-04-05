package com.xpay.gateway.callback.enums;

public enum RespCodeEnum {
    SUCCESS("01", "成功"),
    PARAM_FAIL("02", "验参失败"),
    SIGN_FAIL("03", "验签失败"),
    BIZ_FAIL("04", "业务校验失败"),
    SYS_FORBID("05", "系统限制"),
    SYS_ERROR("06", "系统异常"),
    FALLBACK("07", "系统保护"),
    PATH_ERROR("08", "路径错误"),

    ;

    private String value;
    private String desc;

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    private RespCodeEnum(String value, String desc){
        this.value = value;
        this.desc = desc;
    }
}
