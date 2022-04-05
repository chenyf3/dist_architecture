package com.xpay.facade.message.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.message.dto.PushDto;

/**
 * 移动端消息推送接口
 *
 * @author chenyf
 * @date 2020/3/10
 */
public interface PushFacade {

    /**
     * 消息推送
     * @param pushDto   推送参数
     * @return
     * @throws BizException
     */
    boolean pushMessage(PushDto pushDto) throws BizException;
}
