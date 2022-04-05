package com.xpay.service.message.biz.pusher;

import com.xpay.facade.message.dto.PushDto;

/**
 * 移动推送接口
 */
public interface MobilePusher {

    public boolean pushMessage(PushDto pushDto);
}
