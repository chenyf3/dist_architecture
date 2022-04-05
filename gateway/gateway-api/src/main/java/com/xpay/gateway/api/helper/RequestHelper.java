package com.xpay.gateway.api.helper;

import com.xpay.common.api.params.APIParam;
import com.xpay.common.api.service.MchService;
import com.xpay.common.api.utils.SignUtil;
import com.xpay.common.api.params.MchInfo;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RSAUtil;
import com.xpay.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf
 * @description 签名、验签的组件，需要使用此组件的项目，需要自己配置这个@Bean，同时自己实现UserService、ValidFailService 然后再通过Spring进行依赖注入
 * @date 2021-02-20
 */
public final class RequestHelper {
    private Logger logger = LoggerFactory.getLogger(RequestHelper.class);
    private MchService mchService;

    public RequestHelper(MchService mchService) {
        this.mchService = mchService;
    }

    /**
     * IP校验
     * @param mchNo
     * @param requestIp
     * @param ipValidKey
     * @param param
     * @return
     */
    public boolean ipVerify(String mchNo, String requestIp, String ipValidKey, APIParam param) {
        if (StringUtil.isEmpty(mchNo) || StringUtil.isEmpty(requestIp)) {
            return false;
        }

        MchInfo mchInfo = mchService.getMchInfo(mchNo, param);
        if(mchInfo == null){
            logger.error("mchNo={} 商户记录不存在或获取失败, ip校验不通过！", mchNo);
            return false;
        }

        Map<String, String> ipMap = mchInfo.getIpValidMap();
        if (ipMap == null || ipMap.isEmpty()) { //为空说明当前商户不需要检验IP
            return true;
        }

        if (StringUtil.isEmpty(ipValidKey)) {
            for (Map.Entry<String, String> entry : ipMap.entrySet()) {
                String expectIp = entry.getValue();//预期的Ip
                List<String> expectIps = Arrays.asList(expectIp.split(","));
                if (expectIps.contains(requestIp)) {
                    return true;
                }
            }
            return false;
        } else {
            String expectIp = ipMap == null ? null : ipMap.get(ipValidKey);//预期的Ip
            //为空说明不需要检验IP
            if (StringUtil.isEmpty(expectIp)) {
                return true;
            } else {
                return StringUtil.isNotEmpty(requestIp) && expectIp.contains(requestIp);
            }
        }
    }

    /**
     * 签名校验
     * @param requestParam
     * @return
     */
    public boolean signVerify(RequestParam requestParam, APIParam param) {
        if (requestParam == null || StringUtil.isEmpty(requestParam.getMchNo())) {
            return false;
        }

        MchInfo mchInfo = mchService.getMchInfo(requestParam.getMchNo(), param);
        if(mchInfo == null){
            logger.error("mchNo={} 商户记录不存在或获取失败, 签名校验不通过！", requestParam.getMchNo());
            return false;
        }

        try {
            return SignUtil.verify(requestParam.getSignBody(), requestParam.getSignature(), requestParam.getSignType(), mchInfo.getSignValidKey());
        } catch (Throwable e) {
            logger.error("验签失败，因为验签时出现异常 RequestParam = {} e:", requestParam.toString(), e);
            return false;
        }
    }

    /**
     * 用户信息校验
     * @param mchNo
     * @param param
     * @return
     */
    public boolean mchVerify(String mchNo, APIParam param) {
        if (mchNo == null || StringUtil.isEmpty(mchNo)) {
            return false;
        }

        MchInfo mchInfo = mchService.getMchInfo(mchNo, param);
        if(mchInfo == null){
            logger.error("mchNo={} 商户记录不存在或获取失败, 商户信息校验不通过！", mchNo);
            return false;
        }

        //判断商户的激活状态
        if (PublicStatus.ACTIVE == mchInfo.getMchStatus()) {
            return true;
        }else{
            throw GatewayException.fail(ApiRespCodeEnum.MCH_FAIL.getValue(), "状态受限", GatewayErrorCode.PARAM_CHECK_ERROR);
        }
    }


    /**
     * 给aes_key解密
     */
    public String secKeyDecrypt(String secKeyEnc, String mchNo, APIParam param) {
        if (StringUtil.isEmpty(secKeyEnc) || StringUtil.isEmpty(mchNo)) {
            return null;
        }

        MchInfo mchInfo = mchService.getMchInfo(mchNo, param);
        if (mchInfo == null || StringUtil.isEmpty(mchInfo.getSecKeyDecryptKey())) {
            logger.error("mchNo={} 商户记录不存在或获取失败, 无法为sec_key解密！", mchNo);
            return null;
        }
        //使用平台敏感信息key解密RSA私钥解密
        String secKeyDec = RSAUtil.decryptByPrivateKey(secKeyEnc, mchInfo.getSecKeyDecryptKey());
        return secKeyDec;
    }

    /**
     * 生成签名,如果有secKey，使用商户提供的公钥对其进行加密
     */
    public String secKeyEncrypt(String secKey, String mchNo, APIParam param) {
        if (StringUtil.isEmpty(secKey) || StringUtil.isEmpty(mchNo)) {
            return null;
        }

        MchInfo mchInfo = mchService.getMchInfo(mchNo, param);
        if (mchInfo == null) {
            logger.error("mchNo={} 商户记录不存在或获取失败, 无法为sec_key加密！", mchNo);
            return null;
        }

        if (StringUtil.isNotEmpty(secKey) && StringUtil.isNotEmpty(mchInfo.getSecKeyEncryptKey())) {
            try {
                secKey = RSAUtil.encryptByPublicKey(secKey, mchInfo.getSecKeyEncryptKey());
                return secKey;
            } catch (Exception e) {
                logger.error("secKey加密错误，mchNo:{} APIParam:{}, e:", mchNo, JsonUtil.toJson(param), e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 生成签名
     * @param data
     * @param mchNo
     * @param param
     * @return
     */
    public String genSignature(byte[] data, String mchNo, APIParam param){
        if (StringUtil.isEmpty(mchNo) || StringUtil.isEmpty(param.getSignType())) {
            return null;
        }

        MchInfo mchInfo = mchService.getMchInfo(mchNo, param);
        if(mchInfo == null){
            logger.error("mchNo={} 商户记录不存在或获取失败, 无法进行签名！", mchNo);
            return null;
        }

        try {
            return SignUtil.sign(data, param.getSignType(), mchInfo.getSignGenKey());
        } catch (Throwable e) {
            logger.error("生成签名时出现异常 ResponseParam = {} e:", JsonUtil.toJson(data), e);
            return null;
        }
    }
}
