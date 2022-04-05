package com.xpay.service.merchant.biz;

import com.xpay.common.statics.enums.common.SignTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.*;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.dto.MerchantSecretDto;
import com.xpay.service.merchant.dao.MerchantSecretDao;
import com.xpay.service.merchant.entity.MerchantSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description:商户密钥管理BIZ
 */
@Component
public class MerchantSecretBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MerchantSecretDao merchantSecretDao;

    /**
     * 创建商户私钥记录
     * @param merchantNo
     * @param signTypes
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMerchantSecretKey(String merchantNo, List<Integer> signTypes) throws BizException {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "商户号不能为空");
        }

        List<SecretKey> secretKeys = new ArrayList<>();
        if(signTypes != null){
            for(Integer signType : signTypes){
                if (SignTypeEnum.getEnum(signType) == null) {
                    throw new BizException(BizException.PARAM_INVALID, "不支持的签名方式 signType: " + signType);
                }

                SecretKey secretKey = new SecretKey();
                secretKey.setSignType(signType);
                checkMchPubKeyAndFillPlatKey(secretKey);
                secretKeys.add(secretKey);
            }
        }

        MerchantSecret secret = merchantSecretDao.getByMerchantNo(merchantNo);
        if (secret != null) {
            throw new BizException(BizException.PARAM_INVALID, "商户密钥已存在");
        }

        MerchantSecret newSecret = new MerchantSecret();
        newSecret.setVersion(0);
        newSecret.setCreateTime(new Date());
        newSecret.setModifyTime(newSecret.getCreateTime());
        newSecret.setMchNo(merchantNo);
        newSecret.setSecretKeys(JsonUtil.toJsonWithNull(secretKeys));
        merchantSecretDao.insert(newSecret);
        logger.info("创建商户密钥成功 merchantNo={}", merchantNo);
    }

    /**
     * 根据商户编号获取商户密钥
     * @param merchantNo
     * @return
     * @throws BizException
     */
    public MerchantSecretDto getMerchantSecretByMerchantNo(String merchantNo) throws BizException {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }
        MerchantSecret merchantSecret = merchantSecretDao.getByMerchantNo(merchantNo);
        return BeanUtil.newAndCopy(merchantSecret, MerchantSecretDto.class);
    }

    public SecretKey getSecretKeyByMchNoAndSignType(String merchantNo, Integer signType) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }else if (SignTypeEnum.getEnum(signType) == null) {
            throw new BizException(BizException.PARAM_INVALID, "不支持的签名方式 signType: " + signType);
        }

        List<SecretKey> secretKeys = listSecretKeyByMchNo(merchantNo);
        for(SecretKey secretKey : secretKeys){
            if(signType.equals(secretKey.getSignType())){
                return secretKey;
            }
        }
        return null;
    }

    public List<SecretKey> listSecretKeyByMchNo(String merchantNo){
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }

        MerchantSecretDto mchSecret = getMerchantSecretByMerchantNo(merchantNo);
        List<SecretKey> secretKeys = JsonUtil.toList(mchSecret.getSecretKeys(), SecretKey.class);
        if(secretKeys == null){
            return new ArrayList<>();
        }
        return secretKeys;
    }

    /**
     * 更新商户公钥
     * @param merchantNo
     * @param signType
     * @param mchPublicKey
     */
    public void updateMerchantPublicKey(String merchantNo, Integer signType, String mchPublicKey, boolean isUpdatePlatKey) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }else if(SignTypeEnum.getEnum(signType) == null){
            throw new BizException(BizException.PARAM_INVALID, "无效的签名类型，signType: " + signType);
        }

        MerchantSecret mchSecret = merchantSecretDao.getByMerchantNo(merchantNo);
        if (mchSecret == null) {
            throw new BizException(BizException.BIZ_INVALID, "商户密钥不存在");
        }

        List<SecretKey> secretKeys = JsonUtil.toList(mchSecret.getSecretKeys(), SecretKey.class);
        if(secretKeys == null){
            secretKeys = new ArrayList<>();
        }

        boolean isSignTypeNotExist = true; //当前签名类型是否不存在
        for(SecretKey secretKey : secretKeys){
            if(! signType.equals(secretKey.getSignType())){
                continue;
            }

            isSignTypeNotExist = false;
            secretKey.setMchPublicKey(mchPublicKey);

            if(isUpdatePlatKey){
                checkMchPubKeyAndFillPlatKey(secretKey);
                break;
            }
        }

        if(isSignTypeNotExist){
            SecretKey secretKey = new SecretKey();
            secretKey.setSignType(signType);
            secretKey.setMchPublicKey(mchPublicKey);
            checkMchPubKeyAndFillPlatKey(secretKey);
            secretKeys.add(secretKey);
        }

        mchSecret.setSecretKeys(JsonUtil.toJsonWithNull(secretKeys));
        merchantSecretDao.update(mchSecret);
        logger.info("商户公钥更新成功 merchantNo={} signType={}", merchantNo, signType);
    }

    private void checkMchPubKeyAndFillPlatKey(SecretKey secretKey){
        if (secretKey.getSignType() == SignTypeEnum.RSA.getValue()) {
            if(StringUtil.isNotEmpty(secretKey.getMchPublicKey())
                    && ! RSAUtil.validPublicKey(secretKey.getMchPublicKey())){
                throw new BizException(BizException.PARAM_INVALID, "商户的RSA公钥无效");
            }

            Map<String, String> keyMap = RSAUtil.genKeyPair(true);
            secretKey.setPlatPublicKey(keyMap.get(RSAUtil.PUBLIC_KEY));
            secretKey.setPlatPrivateKey(keyMap.get(RSAUtil.PRIVATE_KEY));
        }else if(secretKey.getSignType() == SignTypeEnum.SM2.getValue()){
            if(StringUtil.isNotEmpty(secretKey.getMchPublicKey())
                    && ! SMUtil.validPublicKey(secretKey.getMchPublicKey())){
                throw new BizException(BizException.PARAM_INVALID, "商户的SM2公钥无效");
            }

            Map<String, String> keyMap = SMUtil.genKeyPair();
            secretKey.setPlatPublicKey(keyMap.get(SMUtil.PUBLIC_KEY));
            secretKey.setPlatPrivateKey(keyMap.get(SMUtil.PRIVATE_KEY));
        }
    }
}
