/*
 * Powered By [xpay.com]
 */
package com.xpay.service.config.dao;

import com.xpay.common.service.dao.MyBatisDao;

import com.xpay.service.config.entity.ProductOpen;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ProductOpenDao extends MyBatisDao<ProductOpen, Long>{

    public boolean isMchProductOpen(String mchNo, Integer productCode){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mchNo", mchNo);
        paramMap.put("productCode", productCode);
        return countBy("countMchProductOpen", paramMap) > 0;
    }
}
