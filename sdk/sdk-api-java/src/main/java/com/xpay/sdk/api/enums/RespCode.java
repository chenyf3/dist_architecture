package com.xpay.sdk.api.enums;

/**
 * 响应码枚举
 */
public enum RespCode {
    SUCCESS("01"),//受理成功
    MCH_ERR("02"),//商户信息错误
    SIGN_FAIL("03"),//验签失败
    PARAM_FAIL("04"),//验参失败
    BIZ_FAIL("05"),//业务规则校验失败
    SYS_FORBID("06"),//系统受限
    UNKNOWN("07"),//受理未知
    ;



    /** 枚举值 */
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private RespCode(String code) {
        this.code = code;
    }
}
