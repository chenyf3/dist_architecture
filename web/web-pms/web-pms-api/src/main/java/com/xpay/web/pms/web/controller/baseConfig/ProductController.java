package com.xpay.web.pms.web.controller.baseConfig;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.base.config.dto.ProductDto;
import com.xpay.facade.base.config.dto.ProductOpenDto;
import com.xpay.facade.base.config.service.ProductFacade;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.facade.merchant.service.MerchantFacade;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.baseConfig.ProductOpenQueryVo;
import com.xpay.web.pms.web.vo.baseConfig.ProductOpenVo;
import com.xpay.web.pms.web.vo.baseConfig.ProductQueryVo;
import com.xpay.web.pms.web.vo.baseConfig.ProductVo;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("baseConfig")
public class ProductController extends BaseController {
    @DubboReference
    MerchantFacade merchantFacade;
    @DubboReference
    ProductFacade productFacade;

    /**
     * 分页查询产品记录
     * @param queryVo
     * @return
     */
    @Permission("baseConfig:product:list")
    @RequestMapping("listProductPage")
    public RestResult<PageResult<List<ProductDto>>> listProductPage(@RequestBody @Valid ProductQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> map = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<ProductDto>> pageResult = productFacade.listProductPage(map, pageQuery);
        return RestResult.success(pageResult);
    }

    /**
     * 添加产品记录
     * @param productVo
     * @return
     */
    @Permission("baseConfig:product:add")
    @RequestMapping("addProduct")
    public RestResult<String> addProduct(@RequestBody @Valid ProductVo productVo){
        boolean isSuccess = productFacade.addProduct(productVo.getProductType(), productVo.getProductCode(), productVo.getRemark());
        return RestResult.success(isSuccess ? "添加成功" : "添加失败");
    }

    /**
     * 编辑产品记录，包括启用、禁用
     * @param productId
     * @param remark
     * @return
     */
    @Permission("baseConfig:product:edit")
    @RequestMapping("editProduct")
    public RestResult<String> editProduct(@RequestParam Long productId, @RequestParam String remark){
        ProductDto product = productFacade.getProductById(productId);
        if(product == null){
            throw new BizException(BizException.BIZ_INVALID, "产品记录不存在！");
        }

        String msg  = "";
        if(product.getStatus() == PublicStatus.ACTIVE){
            boolean isSuccess = productFacade.disableProduct(productId, remark);
            msg = "禁用" + (isSuccess ? "成功" : "失败");
        }else if(product.getStatus() == PublicStatus.INACTIVE){
            boolean isSuccess = productFacade.enableProduct(productId, remark);
            msg = "启用" + (isSuccess ? "成功" : "失败");
        }
        return RestResult.success(msg);
    }

    /**
     * 分页查询产品开通记录
     * @param queryVo
     * @return
     */
    @Permission("baseConfig:productOpen:list")
    @RequestMapping("listProductOpenPage")
    public RestResult<PageResult<List<ProductOpenDto>>> listProductOpenPage(@RequestBody @Valid ProductOpenQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> map = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<ProductOpenDto>> pageResult = productFacade.listProductOpenPage(map, pageQuery);
        return RestResult.success(pageResult);
    }

    /**
     * 添加产品开通
     * @param openVo
     * @return
     */
    @Permission("baseConfig:productOpen:add")
    @RequestMapping("addProductOpen")
    public RestResult<String> addProductOpen(@RequestBody @Valid ProductOpenVo openVo){
        ProductDto product = productFacade.getProductByCode(openVo.getProductCode());
        if(product == null){
            throw new BizException(BizException.BIZ_INVALID, "产品记录不存在，请先添加产品记录！");
        }
        MerchantDto merchant = merchantFacade.getMerchantByMerchantNo(openVo.getMchNo());
        if(merchant == null){
            throw new BizException(BizException.BIZ_INVALID, "商户记录不存在！");
        }

        openVo.setProductType(product.getProductType());
        ProductOpenDto productOpenDto = BeanUtil.newAndCopy(openVo, ProductOpenDto.class);
        boolean isSuccess = productFacade.addProductOpen(productOpenDto);
        return RestResult.success(isSuccess ? "添加成功" : "添加失败");
    }

    /**
     * 添加产品开通时需要搜索商户
     */
    @Permission("baseConfig:productOpen:add")
    @RequestMapping("searchMerchant")
    public RestResult<List<MerchantDto>> searchMerchant(@RequestParam String mchName){
        if(StringUtil.isEmpty(mchName)){
            return RestResult.error("请输入商户名称！");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("fullName", mchName);
        PageQuery pageQuery = PageQuery.newInstance(1, 5);
        PageResult<List<MerchantDto>> pageResult = merchantFacade.listMerchantPage(paramMap, pageQuery);
        return RestResult.success(pageResult.getData());
    }

    /**
     * 编辑产品开通，包括启用、禁用、调整过期时间
     * @return
     */
    @Permission("baseConfig:productOpen:edit")
    @RequestMapping("editProductOpen")
    public RestResult<String> editProductOpen(@RequestBody ProductOpenVo openVo){
        if (openVo.getId() == null) {
            throw new BizException(BizException.BIZ_INVALID, "产品开通记录id不能为空！");
        }

        ProductOpenDto productOpen = productFacade.getProductOpenById(openVo.getId());
        if(productOpen == null){
            throw new BizException(BizException.BIZ_INVALID, "产品开通记录不存在！");
        }

        ProductOpenDto productOpenNew = BeanUtil.newAndCopy(openVo, ProductOpenDto.class);
        boolean isSuccess = productFacade.editProductOpen(productOpenNew);
        return isSuccess ? RestResult.success("操作成功") : RestResult.error("操作失败");
    }
}
