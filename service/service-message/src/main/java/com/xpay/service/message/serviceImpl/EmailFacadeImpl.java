package com.xpay.service.message.serviceImpl;

import com.xpay.facade.message.dto.EmailSendDto;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.service.message.biz.email.EmailBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@DubboService
public class EmailFacadeImpl implements EmailFacade {
    @Autowired
    EmailBiz emailBiz;

    public Map<String, String> getMailSender(){
        return emailBiz.getMailSender();
    }

    public boolean send(String groupKey, String subject, String content){
        return emailBiz.send(groupKey, subject, content);
    }

    public boolean sendAsync(String groupKey, String subject, String content){
        return emailBiz.sendAsync(groupKey, subject, content);
    }

    public boolean sendHtml(String groupKey, String subject, String content){
        return emailBiz.sendHtml(groupKey,subject, content);
    }

    public boolean sendHtmlAsync(String groupKey, String subject, String content){
        return emailBiz.sendHtmlAsync(groupKey, subject, content);
    }

    @Override
    public boolean send(String from, String to, String[] cc, String subject, String content){
        return emailBiz.send(from, to, cc, subject, content);
    }

    @Override
    public boolean sendHtml(String from, String to, String[] cc, String subject, String content){
        return emailBiz.sendHtml(from, to, cc, subject, content);
    }

    @Override
    public boolean sendAsync(String from, String to, String[] cc, String subject, String content){
        return emailBiz.sendAsync(from, to, cc, subject, content, false);
    }

    @Override
    public boolean sendHtmlAsync(String from, String to, String[] cc, String subject, String content){
        return emailBiz.sendAsync(from, to, cc, subject, content, true);
    }

    @Override
    public boolean send(EmailSendDto emailParam){
        return emailBiz.send(emailParam);
    }

    @Override
    public boolean sendAsync(EmailSendDto emailParam){
        return emailBiz.sendAsync(emailParam);
    }
}
