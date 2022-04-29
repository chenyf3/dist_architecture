package com.xpay.service.message.biz.email;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.facade.message.dto.EmailSendDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.EmailMsgDto;
import com.xpay.facade.message.enums.EmailSendStatusEnum;
import com.xpay.service.message.biz.common.TemplateResolver;
import com.xpay.service.message.dao.MailGroupDao;
import com.xpay.service.message.dao.MailDelayRecordDao;
import com.xpay.service.message.entity.MailGroup;
import com.xpay.service.message.entity.MailDelayRecord;
import com.xpay.starter.plugin.client.EmailClient;
import com.xpay.starter.plugin.plugins.MQSender;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class EmailBiz {
    public final static String TEMPLATE_FOLDER = "email" + File.separator;//邮件模版的路径：classpath:templates/email/

    @Autowired
    TemplateResolver templateResolver;
    @Autowired
    EmailClient emailClient;
    @Autowired
    MQSender mqSender;
    @Autowired
    MailGroupDao mailGroupDao;
    @Autowired
    MailDelayRecordDao mailDelayRecordDao;

    /**
     * 同步发送邮件
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean send(String groupKey, String subject, String content){
        ImmutableTriple<String, String[], String[]> sendInfo = getMailSendInfo(groupKey);
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
        ImmutableTriple<String, String[], String[]> sendInfo = getMailSendInfo(groupKey);
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
        ImmutableTriple<String, String[], String[]> sendInfo = getMailSendInfo(groupKey);
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
        ImmutableTriple<String, String[], String[]> sendInfo = getMailSendInfo(groupKey);
        return sendAsync(sendInfo.getLeft(), sendInfo.getMiddle(), sendInfo.getRight(), subject, content, true);
    }

    /**
     * 延迟合并发送邮件，实际发送由 {@link com.xpay.service.message.task.EmailMergeSendTask} 来处理
     * @param groupKey
     * @param subject
     * @param trxNo
     * @param content
     * @return
     */
    public boolean sendHtmlMerge(String groupKey, String subject, String content, String trxNo){
        if(StringUtil.isEmpty(groupKey)){
            throw new BizException("邮件分组不能为空");
        }

        MailGroup mailGroup = mailGroupDao.getByGroupKey(groupKey);
        if (mailGroup == null) {
            throw new BizException("邮件分组配置不存在," + groupKey);
        }

        MailDelayRecord delayRecord = new MailDelayRecord();
        delayRecord.setCreateTime(new Date());
        delayRecord.setCreateDate(delayRecord.getCreateTime());
        delayRecord.setGroupKey(groupKey);
        delayRecord.setSubject(subject);
        delayRecord.setContent(content);
        delayRecord.setTrxNo(trxNo == null ? "" : trxNo);
        delayRecord.setContent(content);
        delayRecord.setStatus(EmailSendStatusEnum.PENDING.getValue());
        delayRecord.setSendTimes(0);
        mailDelayRecordDao.insert(delayRecord);
        return true;
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
        String[] toArr = StringUtil.commaToArray(to);
        return emailClient.sendTextMail(from, toArr, cc, subject, content);
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
    public boolean send(String from, String[] to, String[] cc, String subject, String content){
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
        String[] toArr = new String[]{to};
        return sendAsync(from, toArr, cc, subject, content, isHtml);
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
    public boolean sendAsync(String from, String[] to, String[] cc, String subject, String content, boolean isHtml){
        EmailMsgDto msgDto = new EmailMsgDto();
        msgDto.setTopic(TopicDest.EMAIL_SEND_ASYNC);
        msgDto.setTags(TopicGroup.COMMON_GROUP);
        msgDto.setTrxNo(RandomUtil.get16LenStr());
        msgDto.setFrom(from);
        msgDto.setTo(String.join(",", to));
        msgDto.setCc(cc);
        msgDto.setSubject(subject);
        msgDto.setContent(content);
        msgDto.setHtmlFormat(isHtml);
        return mqSender.sendOne(msgDto);
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
        String[] toArr = StringUtil.commaToArray(to);
        return emailClient.sendHtmlMail(from, toArr, cc, subject, content);
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
    public boolean sendHtml(String from, String[] to, String[] cc, String subject, String content){
        return emailClient.sendHtmlMail(from, to, cc, subject, content);
    }

    /**
     * 同步发送邮件
     * @param emailParam
     * @return
     */
    public boolean send(EmailSendDto emailParam){
        String content = resolveTplContent(emailParam.getTpl(), emailParam.getTplParam());

        String[] toArr = StringUtil.commaToArray(emailParam.getTo());
        if(emailParam.getHtmlFormat()){
            return emailClient.sendHtmlMail(emailParam.getFrom(), toArr, emailParam.getCc(), emailParam.getSubject(), content);
        }else{
            return emailClient.sendTextMail(emailParam.getFrom(), toArr, emailParam.getCc(), emailParam.getSubject(), content);
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
        return mqSender.sendOne(msgDto);
    }

    /**
     * 取得发件人列表
     * @return
     */
    public Map<String, String> getMailSender(){
        return emailClient.getSenderInfo();
    }

    /**
     * 根据模板名称和模板参数解析出模板内容
     * @param tplName
     * @param paramMap
     * @return
     */
    public String resolveTplContent(String tplName, Map<String, Object> paramMap){
        String tplPath = TEMPLATE_FOLDER + tplName;
        return templateResolver.resolve(tplPath, paramMap);
    }

    /**
     * 取得收件人记录
     * @param groupKey  组名
     * @return  左：发件人，中：收件人列表，右：抄送人列表
     */
    private ImmutableTriple<String, String[], String[]> getMailSendInfo(String groupKey){
        MailGroup mailGroup = mailGroupDao.getByGroupKey(groupKey);
        if(mailGroup == null){
            throw new BizException(BizException.BIZ_INVALID, "groupKey=" + groupKey + "对应的收件人记录不存在");
        }

        String from = mailGroup.getSender();
        if(StringUtil.isEmpty(from)){
            throw new BizException(BizException.BIZ_INVALID, "发件人为空");
        }else if(StringUtil.isEmpty(mailGroup.getReceivers())){
            throw new BizException(BizException.BIZ_INVALID, "收件人为空");
        }

        //收件人、抄送人 格式解析
        List<String> toList = JsonUtil.toList(mailGroup.getReceivers(), String.class);
        String[] toArr = new String[toList.size()];
        toList.toArray(toArr);

        List<String> ccList = null;
        if(StringUtil.isNotEmpty(mailGroup.getCc())){
            ccList = JsonUtil.toList(mailGroup.getCc(), String.class);
        }
        String[] ccArr = ccList != null ? new String[ccList.size()] : null;
        if(ccList != null){
            ccList.toArray(ccArr);
        }
        return ImmutableTriple.of(from, toArr, ccArr);
    }

    public List<MailDelayRecord> listPendingDelayRecord(List<String> createDateList, Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
        return mailDelayRecordDao.listPendingRecord(createDateList, endTime, offset, limit, maxSendTimes);
    }

    public List<MailDelayRecord> listSendingOvertimeDelayRecord(List<String> createDateList, Date endTime, Integer offset, Integer limit, Integer maxSendTimes){
        return mailDelayRecordDao.listSendingOvertimeRecord(createDateList, endTime, offset, limit, maxSendTimes);
    }

    public List<MailDelayRecord> listFinishOrOvertimesDelayRecord(Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
        return mailDelayRecordDao.listFinishOrOvertimesRecord(endTime, offset, limit, maxSendTimes);
    }

    /**
     * 把'待发送'更新为'发送中'，并且提供事务处理，保证这一批次记录的原子性
     * @param idList
     * @param sendStartTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePendingDelayRecordToSending(List<Long> idList, Date sendStartTime){
        int successCount = mailDelayRecordDao.updatePendingToSending(idList, sendStartTime);
        if(successCount != idList.size()){
            throw new BizException(BizException.BIZ_INVALID, "实际更新记录数("+successCount+")与预期更新记录数("+idList.size()+")不一致");
        }
    }

    public int revertSendingDelayRecordToPending(List<Long> idList){
        return mailDelayRecordDao.revertSendingToPending(idList);
    }

    public int updateSendingDelayRecordToFinish(List<Long> idList, Date sendFinishTime){
        return mailDelayRecordDao.updateSendingToFinish(idList, sendFinishTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDelayRecord(List<Long> idList){
        mailDelayRecordDao.deleteByIdList(idList);
    }
}
