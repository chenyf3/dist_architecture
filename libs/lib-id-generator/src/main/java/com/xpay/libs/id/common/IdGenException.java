package com.xpay.libs.id.common;

public class IdGenException extends RuntimeException {

    public IdGenException(String msg){
        super(msg);
    }

    public IdGenException(String msg, Throwable e){
        super(msg, e);
    }
}
