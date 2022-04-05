package com.xpay.facade.merchant.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.dto.MerchantSecretDto;

import java.util.List;

/**
 * Description:商户密钥管理接口
 */
public interface MerchantSecretFacade {
    /**
     * 创建商户新密钥
     * @param merchantNo        商户编号
     * @param signTypes         公钥字符串
     */
    void createMerchantSecretKey(String merchantNo, List<Integer> signTypes) throws BizException;

    /**
     * 根据商户号获取密钥
     * @param merchantNo 商户编号
     * @return 商户新密钥
     */
    MerchantSecretDto getMerchantSecretByMerchantNo(String merchantNo) throws BizException;

    /**
     * 根据商户编号和签名类型查询商户的密钥
     * @param merchantNo
     * @param signType
     * @return
     * @throws BizException
     */
    SecretKey getSecretKeyByMchNoAndSignType(String merchantNo, Integer signType) throws BizException;

    /**
     * 更新商户公钥
     * @param merchantNo        商户编号
     * @param signType          签名类型
     * @param mchPublicKey      商户公钥
     * @param isUpdatePlatKey   是否更新平台密钥对
     */
    void updateMerchantPublicKey(String merchantNo, Integer signType, String mchPublicKey, boolean isUpdatePlatKey) throws BizException;

    /**
     * 创建商户交易密码
     * @param merchantNo
     * @param tradePwd
     * @throws BizException
     */
    void createMerchantTradePwd(String merchantNo, String tradePwd) throws BizException;

    /**
     * 更新商户交易密码
     * @param mchNo     商户编号
     * @param newPwd    新密码
     * @param modifier  操作人
     * @param modifier  描述
     * @throws BizException
     */
    boolean updateMerchantTradePwd(String mchNo, String newPwd, String modifier, String remark) throws BizException;

    /**
     * 验证交易密码是否正确
     * @param mchNo
     * @param pwd
     * @return
     */
    public boolean validTradePwd(String mchNo, String pwd);
}