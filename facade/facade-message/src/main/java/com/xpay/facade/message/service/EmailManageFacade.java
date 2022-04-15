package com.xpay.facade.message.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.message.dto.MailGroupDto;

import java.util.List;
import java.util.Map;

/**
 * 信息服务管理接口
 */
public interface EmailManageFacade {
    /**
     * 根据id查询邮件收件人
     * @param id
     * @return
     */
    public MailGroupDto getMailGroupById(Long id);

    /**
     * 录入邮件收件人配置
     * @param mailGroupDto
     * @return
     */
    public boolean addMailGroup(MailGroupDto mailGroupDto) throws BizException;

    /**
     * 修改邮件收件人配置
     * @param mailGroupDto
     * @return
     */
    public boolean editMailGroup(MailGroupDto mailGroupDto) throws BizException;

    /**
     * 删除邮件收件人配置
     * @param id
     * @param operator
     * @return
     */
    public boolean deleteMailGroup(Long id, String operator) throws BizException;

    /**
     * 分页查询邮件收件人
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public PageResult<List<MailGroupDto>> listMailGroupPage(Map<String, Object> paramMap, PageQuery pageQuery);
}
