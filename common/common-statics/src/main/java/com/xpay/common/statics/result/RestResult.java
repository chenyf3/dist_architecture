package com.xpay.common.statics.result;

/**
 * 对前端的通用返回结果
 * @author chenyf
 */
public class RestResult<T> {
    public static final int SUCCESS = 200;//成功
    public static final int TOKEN_INVALID = 201;//token无效或不存在，或者token存储信息与服务端存储的不一致等
    public static final int BIZ_ERROR = 202;//业务错误
    public static final int BIZ_WARN = 203;//业务提示/警告
    public static final int PERMISSION_DENY = 204;//无访问权限
    public static final int SYS_ERROR = 205;//系统异常

    private int code;
    private String msg;
    private T data;

    private RestResult(T data, int code, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public static <T> RestResult<T> deny(String message) {
        return new RestResult<>(null, PERMISSION_DENY, message);
    }

    public static <T> RestResult<T> error(String message) {
        return new RestResult<>(null, BIZ_ERROR, message);
    }

    public static <T> RestResult<T> warn(String message) {
        return new RestResult<>(null, BIZ_WARN, message);
    }

    public static <T> RestResult<T> success(T data) {
        return new RestResult<>(data, SUCCESS, "");
    }

    public static <T> RestResult<T> success(T data, String message) {
        return new RestResult<>(data, SUCCESS, message);
    }

    public static RestResult unAuth(String message) {
        return new RestResult<>(null, PERMISSION_DENY, message);
    }

    public static <T> RestResult<T> sysError(String message) {
        return new RestResult<>(null, SYS_ERROR, message);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static boolean isRestRespCode(int code){
        return SUCCESS <= code && code <= SYS_ERROR;
    }
}
