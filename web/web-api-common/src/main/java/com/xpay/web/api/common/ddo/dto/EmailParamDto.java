package com.xpay.web.api.common.ddo.dto;

import java.util.Map;

public class EmailParamDto {
    /**
     * 发件人
     */
    private String from;
    /**
     * 收件人，必填
     */
    private String to;
    /**
     * 抄送人，若没有则传null
     */
    private String[] cc;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件模版名称
     */
    private String tpl;
    /**
     * 模版内的参数
     */
    private Map<String, Object> tplParam;
    /**
     * 是否发送html格式的邮件
     */
    private boolean htmlFormat;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String[] getCc() {
        return cc;
    }

    public void setCc(String[] cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTpl() {
        return tpl;
    }

    public void setTpl(String tpl) {
        this.tpl = tpl;
    }

    public Map<String, Object> getTplParam() {
        return tplParam;
    }

    public void setTplParam(Map<String, Object> tplParam) {
        this.tplParam = tplParam;
    }

    public boolean getHtmlFormat() {
        return htmlFormat;
    }

    public void setHtmlFormat(boolean htmlFormat) {
        this.htmlFormat = htmlFormat;
    }
}
