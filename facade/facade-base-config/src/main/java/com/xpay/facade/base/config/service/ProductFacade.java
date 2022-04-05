package com.xpay.facade.base.config.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.base.config.dto.ProductDto;
import com.xpay.facade.base.config.dto.ProductOpenDto;

import java.util.List;
import java.util.Map;

public interface ProductFacade {

    public boolean addProduct(Integer productType, Integer productCode, String remark) throws BizException;

    public ProductDto getProductById(Long productId);

    public ProductDto getProductByCode(Integer productCode);

    public boolean disableProduct(Long id, String remark) throws BizException;

    public boolean enableProduct(Long id, String remark) throws BizException;

    public PageResult<List<ProductDto>> listProductPage(Map<String, Object> paramMap, PageQuery pageQuery);

    public boolean addProductOpen(ProductOpenDto productOpenDto) throws BizException;

    public boolean editProductOpen(ProductOpenDto productOpenDto) throws BizException;

    public ProductOpenDto getProductOpenById(Long productOpenId);

    public PageResult<List<ProductOpenDto>> listProductOpenPage(Map<String, Object> paramMap, PageQuery pageQuery);

    public boolean isMchProductOpen(String mchNo, Integer productCode) throws BizException;
}
