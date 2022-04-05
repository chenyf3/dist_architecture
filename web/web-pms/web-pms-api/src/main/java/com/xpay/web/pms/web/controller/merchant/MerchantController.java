package com.xpay.web.pms.web.controller.merchant;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.constants.common.SeqNoKey;
import com.xpay.common.statics.constants.message.EmailSend;
import com.xpay.common.statics.constants.message.EmailTpl;
import com.xpay.common.statics.enums.common.SignTypeEnum;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.facade.merchant.dto.MerchantDetailDto;
import com.xpay.facade.merchant.service.MerchantFacade;
import com.xpay.facade.sequence.service.SequenceFacade;
import com.xpay.facade.user.dto.PortalUserDto;
import com.xpay.facade.user.service.PortalUserFacade;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.ddo.dto.EmailParamDto;
import com.xpay.web.api.common.enums.SmsType;
import com.xpay.web.api.common.manager.CodeManager;
import com.xpay.web.api.common.service.CryptService;
import com.xpay.web.api.common.service.MessageService;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.merchant.MerchantInfoVo;
import com.xpay.web.pms.web.vo.merchant.MerchantQueryVo;
import com.xpay.web.pms.web.vo.merchant.ResetTradePwdInfoVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 商户管理
 */
@RestController
@RequestMapping("merchant")
public class MerchantController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    CodeManager codeManager;
    @Autowired
    CryptService cryptService;
    @Autowired
    MessageService messageService;

    @DubboReference
    MerchantFacade merchantFacade;
    @DubboReference
    SequenceFacade sequenceFacade;
    @DubboReference
    PortalUserFacade portalUserFacade;

    @RequestMapping("listMerchantPage")
    @Permission("merchant:merchant:list")
    public RestResult<PageResult<List<MerchantDto>>> listMerchantPage(@RequestBody @Valid MerchantQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> map = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<MerchantDto>> pageResult = merchantFacade.listMerchantPage(map, pageQuery);
        return RestResult.success(pageResult);
    }

    @Permission("merchant:merchant:add")
    @RequestMapping("addMerchant")
    public RestResult addMerchant(@RequestBody @Valid MerchantInfoVo merchantInfo) {
        String mchNo = sequenceFacade.nextRedisId(SeqNoKey.MCH_NO, 10000000L, SeqNoKey.MCH_NO, false);

        MerchantDto merchant = BeanUtil.newAndCopy(merchantInfo, MerchantDto.class);
        merchant.setCreateTime(new Date());
        merchant.setMchNo(mchNo);

        MerchantDetailDto merchantDetail = BeanUtil.newAndCopy(merchantInfo, MerchantDetailDto.class);
        merchantDetail.setMchNo(mchNo);

        String tradePwd = RandomUtil.genRandomPwd(8);
        String tradePwdEnc = cryptService.encryptSha1(tradePwd);
        Integer signType = SignTypeEnum.RSA.getValue();

        merchantFacade.createMerchant(merchant, merchantDetail, tradePwdEnc, signType);

        this.notifyMerchantAdd(mchNo, merchant.getFullName(), merchantDetail.getBussContactEmail(),
                merchantDetail.getBussMobileNo(), tradePwd);
        return RestResult.success("商户添加成功！");
    }

    @RequestMapping("getMerchantInfo")
    @Permission("merchant:merchant:view")
    public RestResult<MerchantInfoVo> getMerchantInfo(@RequestParam @NotEmpty String mchNo){
        MerchantDto merchant = merchantFacade.getMerchantByMerchantNo(mchNo);
        MerchantDetailDto merchantDetail = merchantFacade.getDetailByMerchantNo(mchNo);

        MerchantInfoVo infoVo = new MerchantInfoVo();
        BeanUtil.copy(merchant, infoVo);
        BeanUtil.copy(merchantDetail, infoVo);
        return RestResult.success(infoVo);
    }

    /**
     * 获取重置密码的相关信息
     * @param mchNo
     * @return
     */
    @RequestMapping("getTradePwdResetInfo")
    @Permission("merchant:pwd:resetTradePwd")
    public RestResult<ResetTradePwdInfoVo> getTradePwdResetInfo(@RequestParam @NotEmpty String mchNo){
        MerchantDto merchant = merchantFacade.getMerchantByMerchantNo(mchNo);
        if (merchant == null) {
            return RestResult.error("当前商户信息不存在");
        }

        PortalUserDto portalUser = portalUserFacade.getAdminUser(merchant.getMchNo());
        if (portalUser == null) {
            return RestResult.error("当前商户还未创建商户后台管理员");
        }

        ResetTradePwdInfoVo infoVo = new ResetTradePwdInfoVo();
        infoVo.setMchNo(mchNo);
        infoVo.setMchName(merchant.getFullName());
        infoVo.setAdminLoginName(portalUser.getLoginName());
        infoVo.setAdminRealName(portalUser.getRealName());
        infoVo.setAdminMobileNo(portalUser.getMobileNo());
        return RestResult.success(infoVo);
    }

    /**
     * 发送重置支付密码的短信验证码
     * @param mchNo
     * @return
     */
    @RequestMapping("sendTradePwdResetCode")
    @Permission("merchant:pwd:resetTradePwd")
    public RestResult<String> sendTradePwdResetCode(@RequestParam @NotEmpty String mchNo){
        if(mchNo == null){
            return RestResult.error("商户编号不能为空");
        }

        String cacheKey = getTradePwdResetCacheKey(mchNo);
        String limitKey = getTradePwdResetLimitKey(mchNo);
        String limitValue = codeManager.getLimitVal(limitKey);
        if(StringUtil.isNotEmpty(limitValue)){
            return RestResult.error("请勿频繁获取验证码！");
        }

        MerchantDetailDto detail = merchantFacade.getDetailByMerchantNo(mchNo);
        if (detail == null) {
            return RestResult.error("当前商户信息不存在");
        }

        PortalUserDto portalUserDto = portalUserFacade.getAdminUser(detail.getMchNo());
        if (portalUserDto == null) {
            return RestResult.error("当前商户还未创建商户后台管理员");
        }

        int expireMin = 10;
        String phoneNo = portalUserDto.getMobileNo();
        String phoneCode = RandomUtil.getDigitStr(4); //随机短信验证码
        codeManager.cacheCode(cacheKey, phoneCode, expireMin * 60, limitKey);

        boolean isSuccess = messageService.sendSmsCode(phoneNo, phoneCode, SmsType.RESET_TRADE_PWD.getValue());
        if(isSuccess){
            return RestResult.success("验证码已发送，" + expireMin + "分钟内有效，" + "请通知商户后台管理员进行重置");
        }else{
            codeManager.deleteCode(cacheKey, limitKey);
            return RestResult.error("短信验证码发送失败，请重试！");
        }
    }

    private void notifyMerchantAdd(String mchNo, String mchName, String email, String mobile, String tradePwd){
        try {
            Map<String, Object> tplParam = new HashMap<>();
            tplParam.put("mchNo", mchNo);
            tplParam.put("mchName", mchName);
            tplParam.put("tradePwd", tradePwd);

            EmailParamDto paramDto = new EmailParamDto();
            paramDto.setFrom(EmailSend.MCH_NOTIFY);
            paramDto.setTo(email);
            paramDto.setSubject("商户创建成功！");
            paramDto.setTpl(EmailTpl.MERCHANT_ADD_TPL);
            paramDto.setTplParam(tplParam);
            boolean isSuccess = messageService.sendEmailAsync(paramDto);
            if(!isSuccess){
                logger.error("商户创建成功，但通知发送失败 mchNo={}", mchNo);
            }
        } catch(Exception e) {
            logger.error("商户创建成功，但通知发送出现异常 mchNo={}", mchNo, e);
        }
    }
    private String getTradePwdResetCacheKey(String merchantNo){
        return Constants.TRADE_PWD_RESET_KEY + merchantNo;
    }
    private String getTradePwdResetLimitKey(String merchantNo){
        return "resetMchTradePwdLimit:" + merchantNo;
    }
}
