package com.xpay.service.message.biz.email;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.enums.message.EmailGroupKeyEnum;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.MailGroupDto;
import com.xpay.service.message.dao.MailGroupDao;
import com.xpay.service.message.entity.MailGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class MailGroupBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MailGroupDao mailGroupDao;

    public MailGroupDto getMailGroupById(Long id){
        if(id == null) return null;
        MailGroup mailGroup = mailGroupDao.getById(id);
        return BeanUtil.newAndCopy(mailGroup, MailGroupDto.class);
    }

    public boolean addMailGroup(MailGroupDto mailGroupDto){
        if(StringUtil.isEmpty(mailGroupDto.getSender())){
            throw new BizException(BizException.BIZ_INVALID, "发件人不能为空");
        }else if(StringUtil.isEmpty(mailGroupDto.getReceivers())){
            throw new BizException(BizException.BIZ_INVALID, "收件人不能为空");
        }

        EmailGroupKeyEnum groupKeyEnum = EmailGroupKeyEnum.getEnum(mailGroupDto.getGroupKey());
        if(groupKeyEnum == null){
            throw new BizException(BizException.BIZ_INVALID, "[" + mailGroupDto.getGroupKey() + "]系统无此业务分组！");
        }

        MailGroup mailGroup = mailGroupDao.getByGroupKey(groupKeyEnum.name());
        if(mailGroup != null){
            throw new BizException(BizException.BIZ_INVALID, "["+groupKeyEnum.name()+"]当前业务分组已存在");
        }

        mailGroupDto.setCreateTime(new Date());
        mailGroupDto.setVersion(0);
        if(mailGroupDto.getRemark() == null){
            mailGroupDto.setRemark("");
        }
        mailGroup = BeanUtil.newAndCopy(mailGroupDto, MailGroup.class);
        mailGroup.setReceivers(JsonUtil.toJson(mailGroup.getReceivers().split(",")));
        if(StringUtil.isNotEmpty(mailGroup.getCc())){
            mailGroup.setCc(JsonUtil.toJson(mailGroup.getCc().split(",")));
        }

        mailGroupDao.insert(mailGroup);
        return mailGroup.getId() != null;
    }

    public boolean editMailGroup(MailGroupDto mailGroupDto){
        MailGroup mailGroup = mailGroupDao.getById(mailGroupDto.getId());
        if(mailGroup == null){
            throw new BizException(BizException.BIZ_INVALID, "记录不存在");
        }

        if(StringUtil.isNotEmpty(mailGroupDto.getSender())){
            mailGroup.setSender(mailGroupDto.getSender());
        }
        if(StringUtil.isNotEmpty(mailGroupDto.getReceivers())){
            mailGroup.setReceivers(JsonUtil.toJson(mailGroupDto.getReceivers().split(",")));
        }
        if(StringUtil.isEmpty(mailGroupDto.getCc())){
            mailGroup.setCc(null);
        }else{
            mailGroup.setCc(JsonUtil.toJson(mailGroupDto.getCc().split(",")));
        }
        if(StringUtil.isNotEmpty(mailGroupDto.getRemark())){
            mailGroup.setRemark(mailGroupDto.getRemark());
        }
        mailGroupDao.update(mailGroup);
        return true;
    }

    public boolean deleteMailGroup(Long id, String operator){
        MailGroup mailGroup = mailGroupDao.getById(id);
        if(mailGroup == null){
            throw new BizException(BizException.BIZ_INVALID, "记录不存在");
        }
        mailGroupDao.deleteById(id);
        logger.info("删除了邮件收发配置 operator={} MailGroup={}", operator, JsonUtil.toJson(mailGroup));
        return true;
    }

    public PageResult<List<MailGroupDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<MailGroup>> result = mailGroupDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), MailGroupDto.class), result);
    }
}
