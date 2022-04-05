package com.xpay.service.merchant.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.merchant.entity.MerchantDetail;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
public class MerchantDetailDao extends MyBatisDao<MerchantDetail, Long> {

    public MerchantDetail getByMerchantNo(String merchantNo) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }
        return getOne(Collections.singletonMap("mchNo", merchantNo));
    }
}
