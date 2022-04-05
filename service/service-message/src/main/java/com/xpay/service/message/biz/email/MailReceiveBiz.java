package com.xpay.service.message.biz.email;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.enums.message.EmailGroupKeyEnum;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.MailReceiverDto;
import com.xpay.service.message.dao.MailReceiverDao;
import com.xpay.service.message.entity.MailReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class MailReceiveBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MailReceiverDao mailReceiverDao;

    public MailReceiverDto getMailReceiverById(Long id){
        if(id == null) return null;
        MailReceiver mailReceiver = mailReceiverDao.getById(id);
        return BeanUtil.newAndCopy(mailReceiver, MailReceiverDto.class);
    }

    public boolean addMailReceiver(MailReceiverDto mailReceiver){
        if(StringUtil.isEmpty(mailReceiver.getSender())){
            throw new BizException(BizException.BIZ_INVALID, "发件人不能为空");
        }

        EmailGroupKeyEnum groupKeyEnum = EmailGroupKeyEnum.getEnum(mailReceiver.getGroupKey());
        if(groupKeyEnum == null){
            throw new BizException(BizException.BIZ_INVALID, "[" + mailReceiver.getGroupKey() + "]系统无此业务分组！");
        }

        MailReceiver receiver = mailReceiverDao.getByGroupKey(groupKeyEnum.name());
        if(receiver != null){
            throw new BizException(BizException.BIZ_INVALID, "["+groupKeyEnum.name()+"]当前业务分组已存在");
        }
        mailReceiver.setCreateTime(new Date());
        if(mailReceiver.getRemark() == null){
            mailReceiver.setRemark("");
        }

        mailReceiverDao.insert(BeanUtil.newAndCopy(mailReceiver, MailReceiver.class));
        return mailReceiver.getId() != null;
    }

    public boolean editMailReceiver(Long id, String from, String to, String remark){
        MailReceiver receiver = mailReceiverDao.getById(id);
        if(receiver == null){
            throw new BizException(BizException.BIZ_INVALID, "记录不存在");
        }
        if(StringUtil.isNotEmpty(from)){
            receiver.setSender(from);
        }
        if(StringUtil.isNotEmpty(to)){
            receiver.setReceivers(to);
        }
        if(StringUtil.isNotEmpty(remark)){
            receiver.setRemark(remark);
        }
        receiver.setCreateTime(null);
        mailReceiverDao.updateIfNotNull(receiver);
        return true;
    }

    public boolean deleteMailReceiver(Long id, String operator){
        MailReceiver receiver = mailReceiverDao.getById(id);
        if(receiver == null){
            throw new BizException(BizException.BIZ_INVALID, "记录不存在");
        }
        mailReceiverDao.deleteById(id);
        logger.info("删除了邮件收发配置 operator={} MailReceiver={}", operator, JsonUtil.toJson(receiver));
        return true;
    }

    public PageResult<List<MailReceiverDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<MailReceiver>> result = mailReceiverDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), MailReceiverDto.class), result);
    }
}
