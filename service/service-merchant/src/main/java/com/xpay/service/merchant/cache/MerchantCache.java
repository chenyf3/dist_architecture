package com.xpay.service.merchant.cache;

import com.xpay.common.utils.StringUtil;
import com.xpay.facade.merchant.dto.MerchantInfoDto;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.service.merchant.biz.MerchantBiz;
import com.xpay.service.merchant.biz.MerchantSecretBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MerchantCache {
    @Autowired
    MerchantBiz merchantBiz;
    @Autowired
    MerchantSecretBiz merchantSecretBiz;

    @Cacheable(value = "merchantInfoCache", key = "methodName + '.' + #mchNo + '.' + #signType")
    public MerchantInfoDto getMerchantInfo(String mchNo, Integer signType) {
        if(StringUtil.isEmpty(mchNo)){
            return null;
        }

        MerchantDto merchant = merchantBiz.getMerchantByMerchantNo(mchNo);
        if(merchant == null){ //商户不存在，直接返回null
            return null;
        }

        MerchantInfoDto merchantInfo = new MerchantInfoDto();
        merchantInfo.setMchNo(merchant.getMchNo());
        merchantInfo.setShortName(merchant.getShortName());
        merchantInfo.setFullName(merchant.getFullName());
        merchantInfo.setMchStatus(merchant.getStatus());

        if(signType != null){
            SecretKey secretKey = merchantSecretBiz.getSecretKeyByMchNoAndSignType(mchNo, signType);
            if(secretKey != null){
                merchantInfo.setSecretKeys(new ArrayList<>());
                merchantInfo.getSecretKeys().add(secretKey);
            }
        }else{
            List<SecretKey> secretKeys = merchantSecretBiz.listSecretKeyByMchNo(mchNo);
            merchantInfo.setSecretKeys(secretKeys);
        }
        return merchantInfo;
    }
}
