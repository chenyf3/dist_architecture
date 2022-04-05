package com.xpay.service.merchant.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.dto.MerchantSecretDto;
import com.xpay.facade.merchant.service.MerchantSecretFacade;
import com.xpay.service.merchant.biz.MerchantSecretBiz;
import com.xpay.service.merchant.biz.MerchantTradePwdBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Description:商户密钥管理服务
 */
@DubboService
public class MerchantSecretFacadeImpl implements MerchantSecretFacade {
    @Autowired
    private MerchantSecretBiz merchantSecretBiz;
    @Autowired
    private MerchantTradePwdBiz merchantTradePwdBiz;

    @Override
    public void createMerchantSecretKey(String merchantNo, List<Integer> signTypes) throws BizException {
        merchantSecretBiz.createMerchantSecretKey(merchantNo, signTypes);
    }

    @Override
    public MerchantSecretDto getMerchantSecretByMerchantNo(String merchantNo) throws BizException {
        return merchantSecretBiz.getMerchantSecretByMerchantNo(merchantNo);
    }

    @Override
    public SecretKey getSecretKeyByMchNoAndSignType(String merchantNo, Integer signType) throws BizException {
        return merchantSecretBiz.getSecretKeyByMchNoAndSignType(merchantNo, signType);
    }

    @Override
    public void updateMerchantPublicKey(String merchantNo, Integer signType, String mchPublicKey, boolean isUpdatePlatKey) throws BizException {
        merchantSecretBiz.updateMerchantPublicKey(merchantNo, signType, mchPublicKey, isUpdatePlatKey);
    }

    @Override
    public void createMerchantTradePwd(String merchantNo, String tradePwd) throws BizException {
        merchantTradePwdBiz.createTradePwd(merchantNo, tradePwd);
    }

    @Override
    public boolean updateMerchantTradePwd(String mchNo, String newPwd, String modifier, String remark) throws BizException {
        return merchantTradePwdBiz.updateTradePwd(mchNo, newPwd, modifier, remark);
    }

    @Override
    public boolean validTradePwd(String mchNo, String pwd) {
        return merchantTradePwdBiz.validTradePwd(mchNo, pwd);
    }
}
