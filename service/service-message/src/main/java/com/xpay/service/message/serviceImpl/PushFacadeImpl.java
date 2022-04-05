package com.xpay.service.message.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.message.dto.PushDto;
import com.xpay.facade.message.service.PushFacade;
import com.xpay.service.message.biz.pusher.PusherBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 移动推送接口实现
 * @date 2020/3/10
 */
@DubboService
public class PushFacadeImpl implements PushFacade {
    @Autowired
    PusherBiz pusherBiz;

    @Override
    public boolean pushMessage(PushDto pushDto) throws BizException {
        return pusherBiz.pushMessage(pushDto);
    }
}
