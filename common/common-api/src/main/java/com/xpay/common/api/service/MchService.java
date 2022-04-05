package com.xpay.common.api.service;

import com.xpay.common.api.params.APIParam;
import com.xpay.common.api.params.MchInfo;

/**
 * 获取商户信息，此处仅定义接口，具体实现需自行完成并配置SpringBean
 * @author chenyf
 * @date 2018-12-15
 */
public interface MchService {
    /**
     * 根据商户编号获取商户信息
     * @param mchNo
     * @return
     */
    public MchInfo getMchInfo(String mchNo, APIParam param);
}
