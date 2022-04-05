package com.xpay.common.service.exceptions;

public class DBException extends RuntimeException {
    public final static int AFFECT_COUNT_NOT_UNIQUE = 100;//影响的记录数不唯一
    public final static int AFFECT_COUNT_NOT_EXPECT = 101;//影响的记录数与预期不符合

    private int code;
    private String msg;

    public DBException(){
        super();
    }

    public DBException(String msg){
        super(msg);
        this.msg = msg;
    }

    public DBException(int code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
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

    public String getErrInfo(){
        return "{\"code\":" + this.code + ",\"msg\":\"" + this.msg + "\"}";
    }
}
