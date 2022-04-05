package com.xpay.service.config.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.base.config.dto.ProductDto;
import com.xpay.facade.base.config.dto.ProductOpenDto;
import com.xpay.facade.base.config.service.ProductFacade;
import com.xpay.service.config.biz.ProductBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class ProductFacadeImpl implements ProductFacade {
    @Autowired
    ProductBiz productBiz;

    @Override
    public boolean addProduct(Integer productType, Integer productCode, String remark) {
        return productBiz.addProduct(productType, productCode, remark);
    }

    @Override
    public ProductDto getProductById(Long productId){
        return productBiz.getProductById(productId);
    }

    @Override
    public ProductDto getProductByCode(Integer productCode) {
        return productBiz.getProductByCode(productCode);
    }

    @Override
    public boolean disableProduct(Long id, String remark) {
        return productBiz.disableProduct(id, remark);
    }

    @Override
    public boolean enableProduct(Long id, String remark) {
        return productBiz.enableProduct(id, remark);
    }

    @Override
    public PageResult<List<ProductDto>> listProductPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return productBiz.listProductPage(paramMap, pageQuery);
    }

    @Override
    public boolean addProductOpen(ProductOpenDto productOpenDto) {
        return productBiz.addProductOpen(productOpenDto);
    }

    @Override
    public boolean editProductOpen(ProductOpenDto productOpenDto) {
        return productBiz.editProductOpen(productOpenDto);
    }

    @Override
    public ProductOpenDto getProductOpenById(Long productOpenId){
        return productBiz.getProductOpenById(productOpenId);
    }

    @Override
    public boolean isMchProductOpen(String mchNo, Integer productCode) {
        return productBiz.isMchProductOpen(mchNo, productCode);
    }

    @Override
    public PageResult<List<ProductOpenDto>> listProductOpenPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return productBiz.listProductOpenPage(paramMap, pageQuery);
    }
}
