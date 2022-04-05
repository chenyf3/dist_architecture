package com.xpay.service.mchnotify.impl;

import com.xpay.common.api.params.APIParam;
import com.xpay.common.api.params.MchInfo;
import com.xpay.common.api.service.MchService;
import com.xpay.facade.merchant.dto.MerchantInfoDto;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.service.MerchantInfoFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class MchServiceImpl implements MchService {
    @DubboReference
    MerchantInfoFacade merchantInfoFacade;

    @Override
    public MchInfo getMchInfo(String mchNo, APIParam param) {
        Integer signType = Integer.valueOf(param.getSignType());
        MerchantInfoDto merchantInfo = merchantInfoFacade.getMerchantInfo(mchNo, signType);

        MchInfo mchInfo = new MchInfo();
        mchInfo.setSignType(param.getSignType());
        mchInfo.setMchNo(merchantInfo.getMchNo());
        mchInfo.setMchName(merchantInfo.getFullName());
        mchInfo.setMchStatus(merchantInfo.getMchStatus());

        if(merchantInfo.getSecretKeys() != null && merchantInfo.getSecretKeys().size() > 0){
            SecretKey secretKey = merchantInfo.getSecretKeys().get(0);
            mchInfo.setSignGenKey(secretKey.getPlatPrivateKey());
            mchInfo.setSignValidKey(secretKey.getMchPublicKey());
            mchInfo.setSecKeyEncryptKey(secretKey.getMchPublicKey());
            mchInfo.setSecKeyDecryptKey(secretKey.getPlatPrivateKey());
        }
        return mchInfo;
    }
}
