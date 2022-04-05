package com.xpay.web.portal.web.controller.merchant;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.*;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.dto.MerchantSecretDto;
import com.xpay.facade.merchant.service.MerchantFacade;
import com.xpay.facade.merchant.service.MerchantSecretFacade;
import com.xpay.facade.message.enums.SmsTemplateEnum;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.ddo.vo.UserVo;
import com.xpay.web.api.common.ddo.dto.SmsParamDto;
import com.xpay.web.api.common.enums.SmsType;
import com.xpay.web.api.common.manager.CodeManager;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.CryptService;
import com.xpay.web.api.common.service.MessageService;
import com.xpay.web.api.common.service.UserService;
import com.xpay.web.portal.web.controller.BaseController;
import com.xpay.web.portal.web.vo.merchant.MchSecKeyRepVo;
import com.xpay.web.portal.web.vo.merchant.ResetPwdVo;
import com.xpay.web.portal.web.vo.merchant.TradePwdVo;
import com.xpay.web.portal.web.vo.merchant.MchSecKeyReqVo;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 商户安全管理控制器
 */
@RestController
@RequestMapping("/merchantSecurity")
public class SecurityController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MessageService messageService;
    @Autowired
    CryptService cryptService;
    @Autowired
    UserService userService;
    @Autowired
    CodeManager codeManager;

    @DubboReference
    MerchantSecretFacade merchantSecretFacade;
    @DubboReference
    MerchantFacade merchantFacade;

    /**
     * 发送修改支付密码的短信验证码
     * @param userModel
     * @return
     */
    @Permission("merchant:security:changeTradePwd")
    @RequestMapping("sendTradePwdCode")
    public RestResult<String> sendTradePwdCode(@CurrentUser UserModel userModel) {
        Integer smsType = SmsType.CHANGE_TRADE_PWD.getValue();
        String phone = userModel.getMobileNo();
        String limitKey = getSmsLimitKey(smsType, phone);
        String str = codeManager.getLimitVal(limitKey);
        if (StringUtil.isNotEmpty(str)) {
            return RestResult.error("请勿频繁请求短信验证码！");
        }

        String code = RandomUtil.getDigitStr(4);
        try {
            boolean isSuccess = messageService.sendSmsCode(phone, code, smsType);
            if (!isSuccess) {
                return RestResult.error("短信验证码发送失败");
            }
        } catch (BizException e) {
            return RestResult.error("短信验证码发送失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("发送修改支付密码的短信验证码异常", e);
            return RestResult.error("短信验证码发送失败，系统异常");
        }

        //缓存验证码
        int validMinutes = 5;
        String cacheKey = getSmsCacheKey(smsType, phone);
        codeManager.cacheCode(cacheKey, code, validMinutes * 60, limitKey);//有效期5分钟
        return RestResult.success("验证码发送成功，" + validMinutes + "分钟内有效");
    }

    /**
     * 修改支付密码
     * @param userModel
     * @param tradePwdVo
     * @return
     */
    @Permission("merchant:security:changeTradePwd")
    @RequestMapping("changeTradePwd")
    public RestResult<String> changeTradePwd(@CurrentUser UserModel userModel, @RequestBody TradePwdVo tradePwdVo) {
        String oldPwd = cryptService.decryptForWeb(tradePwdVo.getOldPwd(), null);
        String newPwd = cryptService.decryptForWeb(tradePwdVo.getNewPwd(), null);
        String confirmPwd = cryptService.decryptForWeb(tradePwdVo.getConfirmPwd(), null);

        if (!ValidateUtil.validPassword(newPwd, 8, 20, true, true, true)) {
            return RestResult.error("支付密码必须由字母、数字、特殊符号组成,8--20位");
        } else if (!newPwd.equals(confirmPwd)) {
            return RestResult.error("新密码和确认密码不一致");
        }

        //校验手机验证码是否正确
        Integer smsType = SmsType.CHANGE_TRADE_PWD.getValue();
        String cacheKey = getSmsCacheKey(smsType, userModel.getMobileNo());
        String limitKey = getSmsLimitKey(smsType, userModel.getMobileNo());
        String code = codeManager.getCode(cacheKey);
        if (StringUtil.isEmpty(code)) {
            return RestResult.error("验证码已失效！");
        } else if (!code.equals(tradePwdVo.getSmsCode())) {
            codeManager.deleteCode(cacheKey, limitKey);
            return RestResult.error("验证码不正确，请重新获取！");
        }

        //校验旧密码是否正确
        String oldPwdEncrypt = cryptService.encryptSha1(oldPwd);
        boolean isPass = merchantSecretFacade.validTradePwd(userModel.getMchNo(), oldPwdEncrypt);
        if (!isPass) {
            return RestResult.error("原支付密码不正确！");
        }

        //校验新旧密码是否一样
        String newPwdEncrypt = cryptService.encryptSha1(newPwd);
        if (oldPwdEncrypt.equals(newPwdEncrypt)) {
            return RestResult.error("新旧密码不能相同！");
        }

        try {
            boolean isSuccess = merchantSecretFacade.updateMerchantTradePwd(userModel.getMchNo(), newPwdEncrypt,
                    userModel.getLoginName(), "");
            if (!isSuccess) {
                return RestResult.error("支付密码修改失败");
            }
        } catch (BizException e) {
            return RestResult.error("修改失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("修改支付密码时出现异常 loginName={} ", userModel.getLoginName(), e);
            return RestResult.error("修改失败，系统异常");
        }

        try {
            logEdit("修改支付密码成功", userModel);
            codeManager.deleteCode(cacheKey, limitKey);
            super.logEdit("修改支付密码成功", userModel);
        } catch (Exception e) {
        }
        return RestResult.success("修改成功");
    }

    /**
     * 重置支付密码（短信验证码需从运营后台触发发送）
     * @param resetVo
     * @param userModel
     * @return
     */
    @RequestMapping("resetTradePwd")
    @Permission("merchant:security:resetTradePwd")
    public RestResult<String> resetTradePwd(@RequestBody @Valid ResetPwdVo resetVo, @CurrentUser UserModel userModel){
        String newPwd = cryptService.decryptForWeb(resetVo.getNewPwd(), null);
        String confirmPwd = cryptService.decryptForWeb(resetVo.getConfirmPwd(), null);
        if (!ValidateUtil.validPassword(newPwd, 8, 20, true, true, true)) {
            return RestResult.error("支付密码必须由字母、数字、特殊符号组成,8--20位");
        } else if (!newPwd.equals(confirmPwd)) {
            return RestResult.error("新密码和确认密码不一致");
        }

        String cacheKey = getTradePwdResetCacheKey(userModel.getMchNo());
        String cacheCode = codeManager.getCode(cacheKey);
        if(StringUtil.isEmpty(cacheCode)){
            return RestResult.error("验证码已过期或不存在！");
        }else if(! cacheCode.equalsIgnoreCase(resetVo.getVerifyCode())){
            return RestResult.error("验证码错误！");
        }

        String newPwdEnc = cryptService.encryptSha1(newPwd);
        boolean isSuccess = merchantSecretFacade.updateMerchantTradePwd(userModel.getMchNo(), newPwdEnc, userModel.getLoginName(), "重置支付密码");
        if(! isSuccess) {
            return RestResult.error("支付密码重置失败");
        }
        //记录系统日志
        logEdit("重置支付密码", userModel);

        //获取商户信息，取得商户全称
        MerchantDto merchantDto = merchantFacade.getMerchantByMerchantNo(userModel.getMchNo());
        String mchFullName = merchantDto.getFullName();

        //获取当前商户的管理员用户，取得管理员用户的手机号
        UserVo userVo = userService.getAdminUser(userModel.getMchNo());
        String phoneNo = userVo.getMobileNo();

        LinkedHashMap<String, Object> tplParam = new LinkedHashMap<>();
        tplParam.put("realName", mchFullName);
        tplParam.put("notifyType", "支付");

        SmsParamDto smsParam = new SmsParamDto();
        smsParam.setPhone(phoneNo);
        smsParam.setTplName(SmsTemplateEnum.PWD_RESET_NOTIFY.name());
        smsParam.setTplParam(tplParam);
        try {
             messageService.sendSms(smsParam);
        } catch(Exception e) {
            logger.error("支付密码重置成功但通知短信发送异常 mchNo={}", userModel.getMchNo(), e);
        }
        return RestResult.success("支付密码重置成功");
    }

    /**
     * 获取公钥(商户公钥+平台公钥)
     *
     * @param userModel
     * @return
     **/
    @GetMapping("/getSecretPublicKey")
    @Permission("merchant:security:secretKeyManage")
    public RestResult getSecretPublicKey(@CurrentUser UserModel userModel) {
        MerchantSecretDto merchantSecret = merchantSecretFacade.getMerchantSecretByMerchantNo(userModel.getMchNo());
        if (merchantSecret == null) {
            return RestResult.success(new ArrayList<>());
        }

        List<SecretKey> secretKeys = JsonUtil.toList(merchantSecret.getSecretKeys(), SecretKey.class);
        if (secretKeys == null) {
            return RestResult.success(new ArrayList<>());
        }

        List<MchSecKeyRepVo> keyList = new ArrayList<>();
        for (SecretKey secretKey : secretKeys) {
            MchSecKeyRepVo repVo = new MchSecKeyRepVo();
            repVo.setSignType(secretKey.getSignType());
            repVo.setMchPublicKey(secretKey.getMchPublicKey());
            repVo.setPlatPublicKey(secretKey.getPlatPublicKey());
            keyList.add(repVo);
        }
        return RestResult.success(keyList);
    }

    /**
     * 发送修改公钥的验证码
     *
     * @param userModel
     * @return
     */
    @RequestMapping("/sendChangeSecKeyCode")
    @Permission("merchant:security:changeSecretKey")
    public RestResult sendChangeSecKeyCode(@CurrentUser UserModel userModel) {
        String phone = userModel.getMobileNo();
        Integer smsType = SmsType.CHANGE_API_SEC_KEY.getValue();

        String limitKey = getSmsLimitKey(smsType, phone);
        String str = codeManager.getLimitVal(limitKey);
        if (StringUtil.isNotEmpty(str)) {
            throw new BizException("请勿频繁请求短信验证码！");
        }

        try {
            String code = RandomUtil.getDigitStr(6);
            boolean isOk = messageService.sendSmsCode(phone, code, smsType);
            if (isOk) {
                //缓存验证码
                int validMinutes = 5;
                String cacheKey = getSmsCacheKey(smsType, phone);
                codeManager.cacheCode(cacheKey, code, validMinutes * 60, limitKey);//有效期5分钟
                return RestResult.success("短信验证码发送成功！");
            } else {
                return RestResult.error("短信验证码发送失败");
            }
        } catch (BizException e) {
            return RestResult.error("短信验证码发送失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("发送修改商户密钥的短信验证码异常", e);
            return RestResult.error("短信验证码发送失败，系统异常");
        }
    }

    /**
     * 更新商户公钥
     */
    @RequestMapping("/changeSecretKey")
    @Permission("merchant:security:changeSecretKey")
    public RestResult changeSecretKey(@RequestBody @Valid MchSecKeyReqVo secKeyReqDTO, @CurrentUser UserModel userModel) {
        String limitKey = getSmsLimitKey(SmsType.CHANGE_API_SEC_KEY.getValue(), userModel.getMobileNo());
        String cacheKey = getSmsCacheKey(SmsType.CHANGE_API_SEC_KEY.getValue(), userModel.getMobileNo());
        String code = codeManager.getLimitVal(cacheKey);
        if (StringUtil.isEmpty(code)) {
            throw new BizException(BizException.BIZ_INVALID, "验证码已失效，请重新获取");
        } else if (!code.equals(secKeyReqDTO.getVerifyCode())) {
            codeManager.deleteCode(cacheKey, limitKey);
            throw new BizException(BizException.BIZ_INVALID, "验证码错误，请重新获取");
        }

        codeManager.deleteCode(cacheKey, limitKey);

        try {
            String mchNo = userModel.getMchNo();
            Integer signType = secKeyReqDTO.getSignType();
            String mchPubKey = secKeyReqDTO.getMchPubKey();
            boolean isUpdatePlatKey = secKeyReqDTO.getUpdatePlatKey() != null && secKeyReqDTO.getUpdatePlatKey();
            merchantSecretFacade.updateMerchantPublicKey(mchNo, signType, mchPubKey, isUpdatePlatKey);

            String logMsg = isUpdatePlatKey ? "商户公钥和平台密钥对更新成功" : "商户公钥更新成功";
            super.logEdit(logMsg, userModel);
            return RestResult.success("操作成功");
        } catch (BizException e) {
            return RestResult.error("操作失败，" + e.getMsg());
        }
    }

    private String getSmsLimitKey(Integer codeType, String phone) {
        return "SmsLimitKey:" + codeType + ":" + phone;
    }
    private String getSmsCacheKey(Integer codeType, String phone) {
        return "SmsCacheKey:" + codeType + ":" + phone;
    }
    private String getTradePwdResetCacheKey(String merchantNo){
        return Constants.TRADE_PWD_RESET_KEY + merchantNo;
    }
}
