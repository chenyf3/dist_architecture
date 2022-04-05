package com.xpay.web.pms.web.controller.baseConfig;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.MailReceiverDto;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.facade.message.service.EmailManageFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.devops.MailQueryVo;
import com.xpay.web.pms.web.vo.devops.MailReceiverVo;
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

    @Permission("baseConfig:mailReceive:list")
    @RequestMapping("listMailReceiver")
    public RestResult<PageResult<List<MailReceiverDto>>> listMailReceiver(@RequestBody MailQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        pageQuery.setIsNeedTotalRecord(true);

        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<MailReceiverDto>> pageResult = emailManageFacade.listMailReceiverPage(paramMap, pageQuery);
        return RestResult.success(pageResult);
    }

    @Permission("baseConfig:mailReceive:manage")
    @RequestMapping("getMailSender")
    public RestResult getMailSender(){
        Map<String, String> map = emailFacade.getMailSender();
        return RestResult.success(map);
    }

    @Permission("baseConfig:mailReceive:manage")
    @RequestMapping("addMailReceiver")
    public RestResult<String> addMailReceiver(@RequestBody MailReceiverVo receiverVo){
        if(StringUtil.isNotEmpty(receiverVo.getReceivers())){
            receiverVo.setReceivers(receiverVo.getReceivers().trim().replace("&quot;", "\""));
        }

        try{
            JsonUtil.toList(receiverVo.getReceivers(), String.class);
        }catch(Exception e){
            return RestResult.error("收件人格式不正确！");
        }

        MailReceiverDto mailReceiver = BeanUtil.newAndCopy(receiverVo, MailReceiverDto.class);

        try{
            emailManageFacade.addMailReceiver(mailReceiver);
            return RestResult.success("添加成功");
        }catch(BizException e){
            return RestResult.error("添加失败，" + e.getMsg());
        }
    }

    @Permission("baseConfig:mailReceive:manage")
    @RequestMapping("editMailReceiver")
    public RestResult<String> editMailReceiver(@RequestBody MailReceiverVo receiverVo){
        if(StringUtil.isNotEmpty(receiverVo.getReceivers())){
            receiverVo.setReceivers(receiverVo.getReceivers().trim().replace("&quot;", "\""));
        }

        try{
            JsonUtil.toList(receiverVo.getReceivers(), String.class);
        }catch(Exception e){
            return RestResult.error("收件人格式不正确！");
        }

        try {
            emailManageFacade.editMailReceiver(receiverVo.getId(), receiverVo.getSender(), receiverVo.getReceivers(), receiverVo.getRemark());
            return RestResult.success("修改成功");
        } catch(BizException e) {
            return RestResult.error(e.getMsg());
        }
    }

    @Permission("baseConfig:mailReceive:manage")
    @RequestMapping("deleteMailReceiver")
    public RestResult<String> deleteMailReceiver(@RequestParam Long recordId, @CurrentUser UserModel userModel){
        boolean isOk = emailManageFacade.deleteMailReceiver(recordId, userModel.getLoginName());
        return isOk ? RestResult.success("删除成功") : RestResult.error("删除失败");
    }
}
