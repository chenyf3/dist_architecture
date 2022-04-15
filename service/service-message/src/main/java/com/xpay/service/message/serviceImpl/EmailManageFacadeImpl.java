package com.xpay.service.message.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.message.dto.MailGroupDto;
import com.xpay.facade.message.service.EmailManageFacade;
import com.xpay.service.message.biz.email.MailGroupBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class EmailManageFacadeImpl implements EmailManageFacade {
    @Autowired
    MailGroupBiz mailGroupConfigBiz;

    public MailGroupDto getMailGroupById(Long id){
        return mailGroupConfigBiz.getMailGroupById(id);
    }

    @Override
    public boolean addMailGroup(MailGroupDto mailReceiver){
        return mailGroupConfigBiz.addMailGroup(mailReceiver);
    }

    @Override
    public boolean editMailGroup(MailGroupDto mailGroupDto){
        return mailGroupConfigBiz.editMailGroup(mailGroupDto);
    }

    @Override
    public boolean deleteMailGroup(Long id, String operator){
        return mailGroupConfigBiz.deleteMailGroup(id, operator);
    }

    @Override
    public PageResult<List<MailGroupDto>> listMailGroupPage(Map<String, Object> paramMap, PageQuery pageQuery){
        return mailGroupConfigBiz.listPage(paramMap, pageQuery);
    }
}
