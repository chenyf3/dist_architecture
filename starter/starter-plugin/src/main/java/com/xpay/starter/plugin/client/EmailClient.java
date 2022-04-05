package com.xpay.starter.plugin.client;

import com.xpay.starter.plugin.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * 邮件发送器
 * @author chenyf
 */
public class EmailClient {
    private final Logger logger = LoggerFactory.getLogger(EmailClient.class);
    private final static LinkedHashMap<String, JavaMailSender> MAIL_SENDER = new LinkedHashMap();
    private final static LinkedHashMap<String, String> SENDER_INFO = new LinkedHashMap<>();

    public EmailClient(LinkedHashMap<String, JavaMailSender> senderMap, LinkedHashMap<String, String> senderInfo){
        MAIL_SENDER.putAll(senderMap);
        SENDER_INFO.putAll(senderInfo);
    }

    public boolean sendTextMail(String from, String to, String[] cc, String subject, String content){
        JavaMailSender sender = getMailSender(from);
        if(sender == null){
            throw new RuntimeException(from + " 没有此邮件发送者的配置");
        }else if(Utils.isEmpty(to)){
            throw new RuntimeException("邮件接收者不能为空");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        if(cc != null && cc.length > 0){
            message.setCc(cc);
        }

        try {
            sender.send(message);
            return true;
        } catch (Throwable e) {
            logger.error("from={} to={} subject={} content={} 发送Text邮件时发生异常", from, to, subject, content, e);
            return false;
        }
    }

    public boolean sendHtmlMail(String from, String to, String[] cc, String subject, String content) {
        JavaMailSender sender = getMailSender(from);
        if (sender == null) {
            throw new RuntimeException(from + " 没有此邮件发送者的配置");
        }else if(Utils.isEmpty(to)){
            throw new RuntimeException("邮件接收者不能为空");
        }

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            if (cc != null && cc.length > 0) {
                messageHelper.setCc(cc);
            }
            sender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("from={} to={} subject={} content={} 发送HTML邮件时发生异常", from, to, subject, content, e);
            return false;
        }
    }

    public Map<String, String> getSenderInfo(){
        LinkedHashMap<String, String> senderInfo = new LinkedHashMap<>();
        senderInfo.putAll(SENDER_INFO);
        return senderInfo;
    }

    private JavaMailSender getMailSender(String from){
        if (Utils.isNotEmpty(from)) {
            return MAIL_SENDER.get(from);
        } else if (!MAIL_SENDER.isEmpty()) {//如果没有指定发件人，就直接取第一个，用作默认的发件人
            return MAIL_SENDER.entrySet().iterator().next().getValue();//取第一个
        } else {
            return null;
        }
    }
}
