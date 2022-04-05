package com.xpay.web.api.common.service;

import com.xpay.web.api.common.ddo.dto.EmailParamDto;
import com.xpay.web.api.common.ddo.dto.SmsParamDto;

public interface MessageService {

    /**
     * 发送短信验证码
     * @param phone         手机号
     * @param code          验证码
     * @param smsType       短信类型（由接口实现方指定具体值及其含义），可参考 {@link com.xpay.web.api.common.enums.SmsType}
     * @return
     */
    public boolean sendSmsCode(String phone, String code, Integer smsType);

    /**
     * 发送短信
     * @param smsParam
     * @return
     */
    public boolean sendSms(SmsParamDto smsParam);

    /**
     * 发送纯文本邮件
     * @param from      发件人
     * @param to        收件人
     * @param cc        抄送
     * @param subject   邮件主题
     * @param content   邮件正文
     * @return
     */
    public boolean sendEmailText(String from, String to, String[] cc, String subject, String content);

    /**
     * 发送HTML邮件
     * @param from      发件人
     * @param to        收件人
     * @param cc        抄送
     * @param subject   邮件主题
     * @param content   邮件正文
     * @return
     */
    public boolean sendEmailHtml(String from, String to, String[] cc, String subject, String content);

    /**
     * 异步发送邮件
     * @param param
     * @return
     */
    public boolean sendEmailAsync(EmailParamDto param);
}
