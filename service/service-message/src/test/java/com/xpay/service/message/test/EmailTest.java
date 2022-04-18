package com.xpay.service.message.test;

import com.xpay.common.statics.constants.message.EmailSend;
import com.xpay.facade.message.dto.EmailSendDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.message.dto.MailGroupDto;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.facade.message.service.EmailManageFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EmailTest extends BaseTestCase {
    private String to = "abc@xxx.com";
    private String[] cc = new String[]{"def@xxx.com"};

    @DubboReference
    EmailFacade emailFacade;
    @DubboReference
    EmailManageFacade messageManageFacade;

    @Ignore
    @Test
    public void addMailReceiver(){
        MailGroupDto mailGroup = new MailGroupDto();
        mailGroup.setSender(EmailSend.MCH_NOTIFY);
        mailGroup.setGroupKey("TEST_GROUP");
        mailGroup.setReceivers(JsonUtil.toJson(Arrays.asList(to)));
        boolean isOk = messageManageFacade.addMailGroup(mailGroup);
        System.out.println("isOk="+isOk);
    }

    @Ignore
    @Test
    public void sendTextEmail(){
        String content = "<h1 style=\"color:red;\">一封测试邮件</h1>";
        emailFacade.send(EmailSend.SYS_ALERT, to, null, "测试service-messages", content);
    }

    @Ignore
    @Test
    public void sendHtmlEmail(){
        String content = "<h1 style=\"color:red;\">一封测试html的邮件</h1>";
        emailFacade.sendHtml(EmailSend.SYS_ALERT, to, null, "测试service-messages", content);
    }

    @Ignore
    @Test
    public void sendFtlEmail(){
        String content = "<h1 style='color:red;'>一封测试ftl的邮件</h1>";

        Map<String, Object> tplParam = new HashMap<>();
        tplParam.put("content", content);

        EmailSendDto mailParam = new EmailSendDto();
        mailParam.setFrom(EmailSend.SYS_ALERT);
        mailParam.setTo(to);
        mailParam.setCc(cc);
        mailParam.setSubject("测试service-messages");
        mailParam.setTpl("common.ftl");
        mailParam.setTplParam(tplParam);
        mailParam.setHtmlFormat(true);

        emailFacade.send(mailParam);
    }

    @Ignore
    @Test
    public void sendFtlEmailAsync(){
        String content = "<h1 style='color:red;'>一封测试async的邮件</h1>";

        Map<String, Object> tplParam = new HashMap<>();
        tplParam.put("content", content);

        EmailSendDto emailParam = new EmailSendDto();
        emailParam.setFrom(EmailSend.SYS_ALERT);
        emailParam.setTo(to);
//        emailParam.setCc(cc);
        emailParam.setSubject("测试service-messages");
        emailParam.setTpl("common.ftl");
        emailParam.setTplParam(tplParam);
        emailParam.setHtmlFormat(true);

        emailFacade.sendAsync(emailParam);

        try{
            Thread.sleep(3000);
        }catch(Exception e){e.printStackTrace();}
    }
}
