package com.xpay.facade.merchant.service;

import com.xpay.facade.merchant.dto.MerchantInfoDto;

public interface MerchantInfoFacade {
    public MerchantInfoDto getMerchantInfo(String mchNo, Integer signType);
}
