package com.xpay.service.merchant.serviceImpl;

import com.xpay.facade.merchant.dto.MerchantInfoDto;
import com.xpay.facade.merchant.service.MerchantInfoFacade;
import com.xpay.service.merchant.cache.MerchantCache;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class MerchantInfoFacadeImpl implements MerchantInfoFacade {
    @Autowired
    MerchantCache merchantCache;

    @Override
    public MerchantInfoDto getMerchantInfo(String mchNo, Integer signType) {
        return merchantCache.getMerchantInfo(mchNo, signType);
    }
}
