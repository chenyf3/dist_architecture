package com.xpay.service.merchant.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.facade.merchant.dto.MerchantDetailDto;
import com.xpay.facade.merchant.service.MerchantFacade;
import com.xpay.service.merchant.biz.MerchantBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Description:商户管理服务
 */
@DubboService
public class MerchantFacadeImpl implements MerchantFacade {
    @Autowired
    private MerchantBiz merchantBiz;

    public PageResult<List<MerchantDto>> listMerchantPage(Map<String, Object> paramMap, PageQuery pageQuery) throws BizException {
        return merchantBiz.listMerchantPage(paramMap, pageQuery);
    }

    public void createMerchant(MerchantDto merchant, MerchantDetailDto merchantDetail, String tradePwd, Integer signType) throws BizException {
        merchantBiz.createMerchant(merchant, merchantDetail, tradePwd, signType);
    }

    public MerchantDto getMerchantByMerchantNo(String merchantNo) throws BizException {
        return merchantBiz.getMerchantByMerchantNo(merchantNo);
    }

    public MerchantDetailDto getDetailByMerchantNo(String merchantNo) throws BizException {
        return merchantBiz.getDetailByMerchantNo(merchantNo);
    }


}
