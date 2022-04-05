package com.xpay.common.statics.exception;

import java.io.Serializable;

/**
 * 业务异常类
 */
public class BizException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = -345568986985960990L;
    /**
     * 系统内部的错误码
     */
    protected int code;
    /**
     * 错误描述
     */
    protected String msg;


    /**
     * 参数校验不通过
     */
    public final static int PARAM_INVALID = 100100001;
    /**
     * 业务流程校验异常
     */
    public final static int BIZ_INVALID = 100100002;
    /**
     * 未预期异常
     */
    public final static int UNEXPECT_ERROR = 100100003;
    /**
     * 可丢弃MQ消息的码
     */
    public final static int MQ_DISCARD = 100100003;

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

    public BizException() {
        super();
    }

    public BizException(String msg) {
        this(-1, msg);
    }

    public BizException(int code, String msg) {
        this(code, msg, null);
    }

    public BizException(String msg, Throwable cause) {
        this(-1, msg, cause);
    }

    public BizException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public static BizException paramException(String msg) {
        return new BizException(PARAM_INVALID, msg);
    }

    public static BizException bizException(String msg) {
        return new BizException(BIZ_INVALID, msg);
    }
    public static BizException unexpectException(String msg) {
        return new BizException(UNEXPECT_ERROR, msg);
    }

    public static BizException mqDiscardException(String msg) {
        return new BizException(MQ_DISCARD, msg);
    }
}
