package com.xpay.facade.message.service;

import com.xpay.facade.message.dto.EmailSendDto;

import java.util.Map;

/**
 * 邮件发送接口
 */
public interface EmailFacade {
    /**
     * 获取邮件发送者信息
     * @return
     */
    public Map<String, String> getMailSender();

    /**
     * 给指定业务组发送邮件(同步发送)
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean send(String groupKey, String subject, String content);

    /**
     * 给指定业务组发送邮件(异步发送)
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendAsync(String groupKey, String subject, String content);

    /**
     * 给指定业务组发送HTML邮件(同步发送)
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendHtml(String groupKey, String subject, String content);

    /**
     * 给指定业务组发送HTML邮件(异步发送)
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendHtmlAsync(String groupKey, String subject, String content);

    /**
     * 给指定业务组发送HTML邮件(合并发送、异步发送)
     * @param groupKey  邮件分组名
     * @param subject   邮件主题
     * @param content   邮件内容
     * @param trxNo     交易流水号(选填)
     * @return
     */
    public boolean sendHtmlMerge(String groupKey, String subject, String content, String trxNo);

    /**
     * 发送邮件(同步发送)
     * @param from      发件人
     * @param to        收件人
     * @param cc        抄送，若没有则传null
     * @param subject   邮件主题
     * @param content   邮件内容
     * @return
     */
    public boolean send(String from, String to, String[] cc, String subject, String content);

    /**
     * 发送邮件(同步发送)
     * @param from
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @return
     */
    public boolean send(String from, String[] to, String[] cc, String subject, String content);

    /**
     * 发送邮件(异步发送)
     * @param from      发件人
     * @param to        收件人
     * @param cc        抄送，若没有则传null
     * @param subject   邮件主题
     * @param content   邮件内容
     * @return
     */
    public boolean sendAsync(String from, String to, String[] cc, String subject, String content);

    /**
     * 发送HTML格式的邮件(同步发送)
     * @param from      发件人
     * @param to        收件人
     * @param cc        抄送，若没有则传null
     * @param subject   邮件主题
     * @param content   邮件内容
     * @return
     */
    public boolean sendHtml(String from, String to, String[] cc, String subject, String content);

    /**
     * 发送HTML格式的邮件(同步发送)
     * @param from
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @return
     */
    public boolean sendHtml(String from, String[] to, String[] cc, String subject, String content);

    /**
     * 发送HTML格式的邮件(异步发送)
     * @param from      发件人
     * @param to        收件人
     * @param cc        抄送，若没有则传null
     * @param subject   邮件主题
     * @param content   邮件内容
     * @return
     */
    public boolean sendHtmlAsync(String from, String to, String[] cc, String subject, String content);

    /**
     * 发送模版邮件(同步发送，使用freemarker根据模版来生成邮件)
     * @param emailParam     邮件参数
     * @return
     */
    public boolean send(EmailSendDto emailParam);

    /**
     * 发送模版邮件(异步发送，使用freemarker根据模版来生成邮件)
     * @param emailParam     邮件参数
     * @return
     */
    public boolean sendAsync(EmailSendDto emailParam);
}
