package com.xpay.facade.extend.vo;

import java.io.Serializable;

public class IdcVo implements Serializable {
    private String code;
    private String name;
    private String desc;

    public IdcVo(){}

    public IdcVo(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
