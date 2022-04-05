package com.xpay.common.exception;

public class UtilException extends RuntimeException {

    public UtilException(String message) {
        super(message);
    }

    public UtilException(String message, Throwable e) {
        super(message, e);
    }
}
