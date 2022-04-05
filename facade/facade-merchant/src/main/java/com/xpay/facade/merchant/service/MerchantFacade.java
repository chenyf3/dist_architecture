package com.xpay.facade.merchant.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.facade.merchant.dto.MerchantDetailDto;

import java.util.List;
import java.util.Map;

/**
 * Description:商户管理服务
 */
public interface MerchantFacade {

    PageResult<List<MerchantDto>> listMerchantPage(Map<String, Object> paramMap, PageQuery pageQuery);

    void createMerchant(MerchantDto merchant, MerchantDetailDto merchantDetail, String tradePwd, Integer signType) throws BizException;

    MerchantDto getMerchantByMerchantNo(String merchantNo);

    MerchantDetailDto getDetailByMerchantNo(String merchantNo);

}
