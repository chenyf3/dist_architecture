package com.xpay.service.message.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.message.dto.MailReceiverDto;
import com.xpay.facade.message.service.EmailManageFacade;
import com.xpay.service.message.biz.email.MailReceiveBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class EmailManageFacadeImpl implements EmailManageFacade {
    @Autowired
    MailReceiveBiz mailReceiveBiz;

    public MailReceiverDto getMailReceiverById(Long id){
        return mailReceiveBiz.getMailReceiverById(id);
    }

    @Override
    public boolean addMailReceiver(MailReceiverDto mailReceiver){
        return mailReceiveBiz.addMailReceiver(mailReceiver);
    }

    @Override
    public boolean editMailReceiver(Long id, String from, String to, String remark){
        return mailReceiveBiz.editMailReceiver(id, from, to, remark);
    }

    @Override
    public boolean deleteMailReceiver(Long id, String operator){
        return mailReceiveBiz.deleteMailReceiver(id, operator);
    }

    @Override
    public PageResult<List<MailReceiverDto>> listMailReceiverPage(Map<String, Object> paramMap, PageQuery pageQuery){
        return mailReceiveBiz.listPage(paramMap, pageQuery);
    }
}
