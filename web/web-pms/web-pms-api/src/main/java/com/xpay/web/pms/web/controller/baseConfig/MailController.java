package com.xpay.web.pms.web.controller.baseConfig;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.MailGroupDto;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.facade.message.service.EmailManageFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.devops.MailQueryVo;
import com.xpay.web.pms.web.vo.devops.MailGroupVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("baseConfig")
public class MailController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    EmailManageFacade emailManageFacade;
    @DubboReference
    EmailFacade emailFacade;

    @Permission("baseConfig:mailGroup:list")
    @RequestMapping("listMailGroup")
    public RestResult<PageResult<List<MailGroupDto>>> listMailGroup(@RequestBody MailQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        pageQuery.setIsNeedTotalRecord(true);

        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<MailGroupDto>> pageResult = emailManageFacade.listMailGroupPage(paramMap, pageQuery);
        if (pageResult.getData() != null && pageResult.getData().size() > 0) {
            pageResult.getData().forEach(group -> {
                if(StringUtil.isNotEmpty(group.getReceivers())){
                    List<String> receivers = JsonUtil.toList(group.getReceivers(), String.class);
                    group.setReceivers(StringUtil.commaToNewline(String.join(",", receivers)));
                }
                if(StringUtil.isNotEmpty(group.getCc())){
                    List<String> cc = JsonUtil.toList(group.getCc(), String.class);
                    group.setCc(StringUtil.commaToNewline(String.join(",", cc)));
                }
            });
        }
        return RestResult.success(pageResult);
    }

    @Permission("baseConfig:mailGroup:manage")
    @RequestMapping("getMailSender")
    public RestResult getMailSender(){
        Map<String, String> map = emailFacade.getMailSender();
        return RestResult.success(map);
    }

    @Permission("baseConfig:mailGroup:manage")
    @RequestMapping("addMailGroup")
    public RestResult<String> addMailGroup(@RequestBody MailGroupVo mailGroupVo){
        mailGroupVo.setReceivers(StringUtil.newlineToComma(mailGroupVo.getReceivers()));
        mailGroupVo.setCc(StringUtil.newlineToComma(mailGroupVo.getCc()));

        MailGroupDto mailGroup = BeanUtil.newAndCopy(mailGroupVo, MailGroupDto.class);

        try {
            emailManageFacade.addMailGroup(mailGroup);
            return RestResult.success("添加成功");
        } catch(BizException e) {
            return RestResult.error("添加失败，" + e.getMsg());
        }
    }

    @Permission("baseConfig:mailGroup:manage")
    @RequestMapping("editMailGroup")
    public RestResult<String> editMailGroup(@RequestBody MailGroupVo mailGroupVo){
        mailGroupVo.setReceivers(StringUtil.newlineToComma(mailGroupVo.getReceivers()));
        mailGroupVo.setCc(StringUtil.newlineToComma(mailGroupVo.getCc()));

        try {
            MailGroupDto mailGroupDto = BeanUtil.newAndCopy(mailGroupVo, MailGroupDto.class);
            emailManageFacade.editMailGroup(mailGroupDto);
            return RestResult.success("修改成功");
        } catch(BizException e) {
            return RestResult.error(e.getMsg());
        }
    }

    @Permission("baseConfig:mailGroup:manage")
    @RequestMapping("deleteMailGroup")
    public RestResult<String> deleteMailGroup(@RequestParam Long recordId, @CurrentUser UserModel userModel){
        boolean isOk = emailManageFacade.deleteMailGroup(recordId, userModel.getLoginName());
        return isOk ? RestResult.success("删除成功") : RestResult.error("删除失败");
    }
}
