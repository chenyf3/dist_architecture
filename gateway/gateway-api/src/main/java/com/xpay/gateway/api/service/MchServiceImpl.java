package com.xpay.gateway.api.service;

import com.google.common.cache.Cache;
import com.xpay.common.api.params.APIParam;
import com.xpay.common.api.params.MchInfo;
import com.xpay.common.api.service.MchService;
import com.xpay.facade.merchant.dto.MerchantInfoDto;
import com.xpay.facade.merchant.dto.SecretKey;
import com.xpay.facade.merchant.service.MerchantInfoFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 获取商户信息的实现类，为提高性能，建议本地加入缓存
 * @author: chenyf
 */
@Component
public class MchServiceImpl implements MchService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    private Cache<String, MchInfo> localCache;//使用本地缓存，避免网络传输的开销
    @DubboReference
    MerchantInfoFacade merchantInfoFacade;

    /**
     * 根据商户编号获取商户信息
     * @param mchNo
     * @return
     */
    @Override
    public MchInfo getMchInfo(String mchNo, APIParam param){
        try{
            String key = getCacheKey(mchNo, param.getSignType());
            MchInfo mchInfo = this.getFromCache(key);
            if(mchInfo == null){
                mchInfo = this.getFromBizService(mchNo, param);
                this.storeToCache(key, mchInfo);
            }
            return mchInfo;
        }catch(Throwable e){
            logger.error("mchNo={} signType={} 获取商户信息时出现异常，将返回NULL值", mchNo, param.getSignType(), e);
            return null;
        }
    }

    private String getCacheKey(String mchNo, String signType){
        return mchNo + "_" + signType;
    }

    private MchInfo getFromCache(String key){
        return localCache != null ? localCache.getIfPresent(key) : null;
    }

    private void storeToCache(String key, MchInfo mchInfo) {
        if (localCache != null && mchInfo != null) {
            localCache.put(key, mchInfo);
        }
    }

    /**
     * 从业务服务获取商户信息，可通过 http、dubbo 等等的方式来查询到商户信息，请根据自身的业务技术架构情况来决定
     * @param mchNo
     * @param param
     * @return
     */
    private MchInfo getFromBizService(String mchNo, APIParam param) {
        Integer signType = Integer.valueOf(param.getSignType());
        MerchantInfoDto merchantInfo = merchantInfoFacade.getMerchantInfo(mchNo, signType);
        if(merchantInfo == null){ //商户记录不存在
            return null;
        }

        MchInfo mchInfo = new MchInfo();
        mchInfo.setSignType(param.getSignType());
        mchInfo.setMchNo(merchantInfo.getMchNo());
        mchInfo.setMchName(merchantInfo.getFullName());
        mchInfo.setMchStatus(merchantInfo.getMchStatus());

        if(merchantInfo.getSecretKeys() != null && merchantInfo.getSecretKeys().size() > 0){
            SecretKey secretKey = merchantInfo.getSecretKeys().get(0);
            mchInfo.setSignGenKey(secretKey.getPlatPrivateKey());
            mchInfo.setSignValidKey(secretKey.getMchPublicKey());
            mchInfo.setSecKeyEncryptKey(secretKey.getMchPublicKey());
            mchInfo.setSecKeyDecryptKey(secretKey.getPlatPrivateKey());
        }
        return mchInfo;
    }
}
