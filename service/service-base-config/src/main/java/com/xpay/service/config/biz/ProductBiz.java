package com.xpay.service.config.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.product.ProductCodeEnum;
import com.xpay.common.statics.enums.product.ProductTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.base.config.dto.ProductDto;
import com.xpay.facade.base.config.dto.ProductOpenDto;
import com.xpay.service.config.dao.ProductDao;
import com.xpay.service.config.dao.ProductOpenDao;
import com.xpay.service.config.entity.Product;
import com.xpay.service.config.entity.ProductOpen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProductBiz {
    @Autowired
    ProductDao productDao;
    @Autowired
    ProductOpenDao productOpenDao;

    /**
     * 添加产品
     * @param productType
     * @param productCode
     * @return
     */
    public boolean addProduct(Integer productType, Integer productCode, String remark){
        if (productType == null) {
            throw new BizException(BizException.BIZ_INVALID, "productType不能为空！");
        }else if (productCode == null){
            throw new BizException(BizException.BIZ_INVALID, "productCode不能为空！");
        }else if(ProductTypeEnum.getEnum(productType) == null){
            throw new BizException(BizException.BIZ_INVALID, "对应的productType不存在！");
        }else if(ProductCodeEnum.getEnum(productCode) == null){
            throw new BizException(BizException.BIZ_INVALID, "对应的productCode不存在！");
        }else if(ProductCodeEnum.getEnum(productCode).getType() != productType){
            throw new BizException(BizException.BIZ_INVALID, "对应的productType和productCode关系不匹配！");
        }else if(getProductByCode(productCode) != null){
            throw new BizException(BizException.BIZ_INVALID, "productCode对应的产品记录已存在！");
        }

        Product product = new Product();
        product.setCreateTime(new Date());
        product.setVersion(0);
        product.setProductType(productType);
        product.setProductCode(productCode);
        product.setStatus(PublicStatus.ACTIVE);
        product.setRemark(remark == null ? "" : StringUtil.subLeft(remark, 200));
        productDao.insert(product);
        return true;
    }

    /**
     * 根据id查询产产品记录
     * @param productId
     * @return
     */
    public ProductDto getProductById(Long productId){
        if(productId == null){
            return null;
        }
        Product product = productDao.getById(productId);
        return BeanUtil.newAndCopy(product, ProductDto.class);
    }

    /**
     * 根据产品编号取得产品记录
     * @param productCode
     * @return
     */
    public ProductDto getProductByCode(Integer productCode){
        if(productCode == null){
            return null;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("productCode", productCode);
        Product product = productDao.getOne(paramMap);
        return BeanUtil.newAndCopy(product, ProductDto.class);
    }

    /**
     * 禁用产品
     * @param id
     * @return
     */
    public boolean disableProduct(Long id, String remark){
        if(id == null){
            throw new BizException(BizException.BIZ_INVALID, "id不能为空");
        }else if(StringUtil.isEmpty(remark)){
            throw new BizException(BizException.BIZ_INVALID, "remark不能为空");
        }

        Product product = productDao.getById(id);
        if(product == null){
            throw new BizException(BizException.BIZ_INVALID, "当前产品记录不存在");
        }else if(product.getStatus() == PublicStatus.INACTIVE){
            return true;
        }

        product.setStatus(PublicStatus.INACTIVE);
        product.setRemark(StringUtil.subLeft(remark, 200));
        productDao.update(product);
        return true;
    }

    /**
     * 启用产品
     * @param id
     * @return
     */
    public boolean enableProduct(Long id, String remark){
        if(id == null){
            throw new BizException(BizException.BIZ_INVALID, "id不能为空");
        }

        Product product = productDao.getById(id);
        if(product == null){
            throw new BizException(BizException.BIZ_INVALID, "当前产品记录不存在");
        }else if(product.getStatus() == PublicStatus.ACTIVE){
            return true;
        }

        product.setStatus(PublicStatus.ACTIVE);
        product.setRemark(remark == null ? "" : StringUtil.subLeft(remark, 200));
        productDao.update(product);
        return true;
    }

    public PageResult<List<ProductDto>> listProductPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<Product>> result = productDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), ProductDto.class), result);
    }

    /**
     * 添加产品开通
     * @param productOpenDto
     * @return
     */
    public boolean addProductOpen(ProductOpenDto productOpenDto){
        if (productOpenDto == null) {
            throw new BizException(BizException.BIZ_INVALID, "产品开通对象不能为空");
        }else if(productOpenDto.getProductCode() == null){
            throw new BizException(BizException.BIZ_INVALID, "productCode不能为空");
        }else if(productOpenDto.getExpireDate() == null){
            throw new BizException(BizException.BIZ_INVALID, "expireDate不能为空");
        }else if(StringUtil.isEmpty(productOpenDto.getMchNo())){
            throw new BizException(BizException.BIZ_INVALID, "mchNo不能为空");
        }

        ProductDto product = getProductByCode(productOpenDto.getProductCode());
        if(product == null){
            throw new BizException(BizException.BIZ_INVALID, "当前产品记录不存在");
        }else if(getProductOpenByMchNoAndCode(productOpenDto.getMchNo(), product.getProductCode()) != null){
            throw new BizException(BizException.BIZ_INVALID, "当前产品开通记录已存在");
        }

        ProductOpen productOpen = BeanUtil.newAndCopy(productOpenDto, ProductOpen.class);
        productOpen.setCreateTime(new Date());
        productOpen.setVersion(0);
        productOpen.setProductType(product.getProductType());
        productOpen.setStatus(PublicStatus.ACTIVE);
        productOpen.setRemark(productOpen.getRemark() == null ? "" : StringUtil.subLeft(productOpen.getRemark(), 200));
        productOpenDao.insert(productOpen);
        return true;
    }

    /**
     * 编辑产品开通
     * @param productOpenDto
     * @return
     */
    public boolean editProductOpen(ProductOpenDto productOpenDto) {
        if(productOpenDto == null){
            throw new BizException(BizException.BIZ_INVALID, "产品开通对象不能为空");
        }else if(productOpenDto.getId() == null){
            throw new BizException(BizException.BIZ_INVALID, "产品开通id不能为空");
        }else if(StringUtil.isEmpty(productOpenDto.getRemark())){
            throw new BizException(BizException.BIZ_INVALID, "remark不能为空");
        }

        if (productOpenDto.getStatus() != null && (productOpenDto.getStatus() != PublicStatus.ACTIVE
                && productOpenDto.getStatus() != PublicStatus.INACTIVE)) {
            throw new BizException(BizException.BIZ_INVALID, "状态值设置不合理");
        }

        ProductOpen productOpen = productOpenDao.getById(productOpenDto.getId());
        if(productOpen == null){
            throw new BizException(BizException.BIZ_INVALID, "当前产品开通记录不存在");
        }

        productOpen.setRemark(StringUtil.subLeft(productOpenDto.getRemark(), 200));
        if (productOpenDto.getStatus() != null) {//status不为空，说明有调整产品开通状态
            productOpen.setStatus(productOpenDto.getStatus());
        }

        //expireTime不为空，说明有调整开品开通的过期时间
        if (productOpenDto.getExpireDate() != null) {
            if (DateUtil.compare(productOpenDto.getExpireDate(), new Date(), Calendar.DATE) < 0) {
                throw new BizException(BizException.BIZ_INVALID, "过期时间不能早于当前时间");
            }
            productOpen.setExpireDate(productOpenDto.getExpireDate());
        }
        productOpenDao.update(productOpen);
        return true;
    }

    /**
     * 根据id查询产品开通记录
     * @param productOpenId
     * @return
     */
    public ProductOpenDto getProductOpenById(Long productOpenId){
        if(productOpenId == null){
            return null;
        }

        ProductOpen productOpen = productOpenDao.getById(productOpenId);
        return BeanUtil.newAndCopy(productOpen, ProductOpenDto.class);
    }

    /**
     * 根据商户编号和产品编码查询产品开通记录
     * @param mchNo
     * @param productCode
     * @return
     */
    public ProductOpenDto getProductOpenByMchNoAndCode(String mchNo, Integer productCode){
        if(StringUtil.isEmpty(mchNo) || productCode == null){
            return null;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mchNo", mchNo);
        paramMap.put("productCode", productCode);
        ProductOpen productOpen = productOpenDao.getOne(paramMap);
        return BeanUtil.newAndCopy(productOpen, ProductOpenDto.class);
    }

    /**
     * 判断商户是否有开通某个产品
     * @param mchNo
     * @param productCode
     * @return
     */
    public boolean isMchProductOpen(String mchNo, Integer productCode){
        if(StringUtil.isEmpty(mchNo)){
            throw new BizException(BizException.BIZ_INVALID, "mchNo不能为空");
        }else if(productCode == null){
            throw new BizException(BizException.BIZ_INVALID, "productCode不能为空");
        }
        return productOpenDao.isMchProductOpen(mchNo, productCode);
    }

    public PageResult<List<ProductOpenDto>> listProductOpenPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<ProductOpen>> result = productOpenDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), ProductOpenDto.class), result);
    }
}
