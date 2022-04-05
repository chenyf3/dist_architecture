package com.xpay.service.message.biz.email;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.facade.message.dto.EmailSendDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.EmailMsgDto;
import com.xpay.service.message.biz.common.TemplateResolver;
import com.xpay.service.message.dao.MailReceiverDao;
import com.xpay.service.message.entity.MailReceiver;
import com.xpay.starter.plugin.client.EmailClient;
import com.xpay.starter.plugin.plugins.MQSender;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class EmailBiz {
    private final static String TEMPLATE_FOLDER = "email" + File.separator;//邮件模版的路径：classpath:templates/email/
    @Autowired
    MailReceiverDao mailReceiverDao;

    @Autowired
    EmailClient emailClient;
    @Autowired
    TemplateResolver templateResolver;
    @Autowired
    MQSender amqSender;

    /**
     * 同步发送邮件
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean send(String groupKey, String subject, String content){
        ImmutableTriple<String, String, String[]> sendInfo = getMailSendInfo(groupKey);
        return send(sendInfo.getLeft(), sendInfo.getMiddle(), sendInfo.getRight(), subject, content);
    }

    /**
     * 异步发送邮件
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendAsync(String groupKey, String subject, String content){
        ImmutableTriple<String, String, String[]> sendInfo = getMailSendInfo(groupKey);
        return sendAsync(sendInfo.getLeft(), sendInfo.getMiddle(), sendInfo.getRight(), subject, content, false);
    }

    /**
     * 同步发送html格式的邮件
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendHtml(String groupKey, String subject, String content){
        ImmutableTriple<String, String, String[]> sendInfo = getMailSendInfo(groupKey);
        return sendHtml(sendInfo.getLeft(), sendInfo.getMiddle(), sendInfo.getRight(), subject, content);
    }

    /**
     * 异步发送html格式的邮件
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendHtmlAsync(String groupKey, String subject, String content){
        ImmutableTriple<String, String, String[]> sendInfo = getMailSendInfo(groupKey);
        return sendAsync(sendInfo.getLeft(), sendInfo.getMiddle(), sendInfo.getRight(), subject, content, true);
    }

    /**
     * 同步发送文本邮件
     * @param from
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @return
     */
    public boolean send(String from, String to, String[] cc, String subject, String content){
        return emailClient.sendTextMail(from, to, cc, subject, content);
    }

    /**
     * 异步发送邮件
     * @param from
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @param isHtml
     * @return
     */
    public boolean sendAsync(String from, String to, String[] cc, String subject, String content, boolean isHtml){
        EmailMsgDto msgDto = new EmailMsgDto();
        msgDto.setTopic(TopicDest.EMAIL_SEND_ASYNC);
        msgDto.setTags(TopicGroup.COMMON_GROUP);
        msgDto.setTrxNo(RandomUtil.get16LenStr());
        msgDto.setFrom(from);
        msgDto.setTo(to);
        msgDto.setCc(cc);
        msgDto.setSubject(subject);
        msgDto.setContent(content);
        msgDto.setHtmlFormat(isHtml);
        return amqSender.sendOne(msgDto);
    }

    /**
     * 同步发送html格式的邮件
     * @param from
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @return
     */
    public boolean sendHtml(String from, String to, String[] cc, String subject, String content){
        return emailClient.sendHtmlMail(from, to, cc, subject, content);
    }

    /**
     * 同步发送邮件
     * @param emailParam
     * @return
     */
    public boolean send(EmailSendDto emailParam){
        String content = templateResolver.resolve(TEMPLATE_FOLDER + emailParam.getTpl(), emailParam.getTplParam());

        if(emailParam.getHtmlFormat()){
            return emailClient.sendHtmlMail(emailParam.getFrom(), emailParam.getTo(), emailParam.getCc(), emailParam.getSubject(), content);
        }else{
            return emailClient.sendTextMail(emailParam.getFrom(), emailParam.getTo(), emailParam.getCc(), emailParam.getSubject(), content);
        }
    }

    /**
     * 异步发送邮件
     * @param emailParam
     * @return
     */
    public boolean sendAsync(EmailSendDto emailParam){
        EmailMsgDto msgDto = new EmailMsgDto();
        msgDto.setTopic(TopicDest.EMAIL_SEND_ASYNC);
        msgDto.setTags(TopicGroup.COMMON_GROUP);
        msgDto.setTrxNo(RandomUtil.get16LenStr());
        msgDto.setFrom(emailParam.getFrom());
        msgDto.setTo(emailParam.getTo());
        msgDto.setCc(emailParam.getCc());
        msgDto.setSubject(emailParam.getSubject());
        msgDto.setTpl(emailParam.getTpl());
        msgDto.setTplParam(emailParam.getTplParam());
        msgDto.setHtmlFormat(emailParam.getHtmlFormat());
        return amqSender.sendOne(msgDto);
    }

    /**
     * 同步发送邮件
     * @param msgDto
     * @return
     */
    public boolean send(EmailMsgDto msgDto){
        String content;
        if(StringUtil.isNotEmpty(msgDto.getTpl())){
            content = templateResolver.resolve(TEMPLATE_FOLDER + msgDto.getTpl(), msgDto.getTplParam());
        }else{
            content = msgDto.getContent();
        }

        if(msgDto.getHtmlFormat()){
            return emailClient.sendHtmlMail(msgDto.getFrom(), msgDto.getTo(), msgDto.getCc(), msgDto.getSubject(), content);
        }else{
            return emailClient.sendTextMail(msgDto.getFrom(), msgDto.getTo(), msgDto.getCc(), msgDto.getSubject(), content);
        }
    }

    /**
     * 取得发件人列表
     * @return
     */
    public Map<String, String> getMailSender(){
        return emailClient.getSenderInfo();
    }

    /**
     * 取得收件人记录
     * @param groupKey  组名
     * @return  左：发件人，中：收件人，右：抄送人列表
     */
    private ImmutableTriple<String, String, String[]> getMailSendInfo(String groupKey){
        MailReceiver mailReceiver = mailReceiverDao.getByGroupKey(groupKey);
        if(mailReceiver == null){
            throw new BizException(BizException.BIZ_INVALID, "groupKey=" + groupKey + "对应的收件人记录不存在");
        }

        String from = mailReceiver.getSender();
        List<String> toList = JsonUtil.toList(mailReceiver.getReceivers(), String.class);
        if(StringUtil.isEmpty(from)){
            throw new BizException(BizException.BIZ_INVALID, "发件人为空");
        }else if(toList == null || toList.isEmpty()){
            throw new BizException(BizException.BIZ_INVALID, "收件人为空");
        }

        //把第一个作为收件人，其他作为抄送人
        String to = null;
        String[] ccArray = toList.size() <= 1 ? null : new String[toList.size() - 1];
        for(int i=0; i<toList.size(); i++){
            if (i == 0) {
                to = toList.get(0);
            } else {
                ccArray[i-1] = toList.get(i);
            }
        }

        return ImmutableTriple.of(from, to, ccArray);
    }
}
