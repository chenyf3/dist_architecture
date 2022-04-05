package com.xpay.service.merchant.biz;

import com.xpay.common.statics.enums.merchant.MerchantStatusEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.merchant.dto.MerchantDto;
import com.xpay.facade.merchant.dto.MerchantDetailDto;
import com.xpay.service.merchant.dao.MerchantDao;
import com.xpay.service.merchant.dao.MerchantDetailDao;
import com.xpay.service.merchant.entity.Merchant;
import com.xpay.service.merchant.entity.MerchantDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商户管理
 */
@Component
public class MerchantBiz {

    @Autowired
    private MerchantDao merchantDao;
    @Autowired
    private MerchantDetailDao merchantDetailDao;
    @Autowired
    private MerchantSecretBiz merchantSecretBiz;
    @Autowired
    private MerchantTradePwdBiz merchantTradePwdBiz;

    /**
     * 分页查询
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public PageResult<List<MerchantDto>> listMerchantPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<Merchant>> result = merchantDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), MerchantDto.class), result);
    }

    /**
     * 创建商户信息
     * @param merchant       商户基本信息
     * @param merchantDetail 商户详细信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMerchant(MerchantDto merchant, MerchantDetailDto merchantDetail, String tradePwd, Integer signType) {
        if(StringUtil.isEmpty(merchant.getMchNo())){
            throw new BizException(BizException.PARAM_INVALID, "mchNo不能为空");
        }else if(merchant.getMchType() == null){
            throw new BizException(BizException.PARAM_INVALID, "mchType不能为空");
        }else if(StringUtil.isEmpty(merchant.getFullName())){
            throw new BizException(BizException.PARAM_INVALID, "fullName不能为空");
        }else if(StringUtil.isEmpty(merchant.getShortName())){
            throw new BizException(BizException.PARAM_INVALID, "shortName不能为空");
        }

        if(getDetailByMerchantNo(merchant.getMchNo()) != null){
            throw new BizException(BizException.PARAM_INVALID, "当前商户编号对应的商户记录已存在！");
        }

        merchant.setCreateTime(new Date());
        merchant.setVersion(0);
        if(merchant.getStatus() == null){
            merchant.setStatus(MerchantStatusEnum.ACTIVE.getValue());
        }

        merchantDetail.setMchNo(merchant.getMchNo());
        merchantDetail.setCreateTime(merchant.getCreateTime());
        merchantDetail.setVersion(0);
        merchantDetail.setModifyTime(merchantDetail.getCreateTime());
        if(StringUtil.isEmpty(merchantDetail.getAddress())){
            merchantDetail.setAddress("");
        }
        if(StringUtil.isEmpty(merchantDetail.getUrl())){
            merchantDetail.setUrl("");
        }
        if(StringUtil.isEmpty(merchantDetail.getIcp())){
            merchantDetail.setIcp("");
        }
        if(StringUtil.isEmpty(merchantDetail.getTelephone())){
            merchantDetail.setTelephone("");
        }
        if(StringUtil.isEmpty(merchantDetail.getBussContactEmail())){
            merchantDetail.setBussContactEmail("");
        }

        merchantDao.insert(BeanUtil.newAndCopy(merchant, Merchant.class));
        merchantDetailDao.insert(BeanUtil.newAndCopy(merchantDetail, MerchantDetail.class));
        merchantTradePwdBiz.createTradePwd(merchant.getMchNo(), tradePwd);
        merchantSecretBiz.createMerchantSecretKey(merchant.getMchNo(), Collections.singletonList(signType));
    }

    public MerchantDto getMerchantByMerchantNo(String merchantNo) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }
        Merchant merchant = merchantDao.getByMerchantNo(merchantNo);
        return BeanUtil.newAndCopy(merchant, MerchantDto.class);
    }

    public MerchantDetailDto getDetailByMerchantNo(String merchantNo) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }

        MerchantDetail detail = merchantDetailDao.getByMerchantNo(merchantNo);
        return BeanUtil.newAndCopy(detail, MerchantDetailDto.class);
    }

}
