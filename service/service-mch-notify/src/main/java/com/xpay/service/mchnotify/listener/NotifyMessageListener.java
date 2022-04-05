package com.xpay.service.mchnotify.listener;

import com.xpay.common.api.dto.CallbackDto;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.utils.JsonUtil;
import com.xpay.service.mchnotify.biz.NotifyBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class NotifyMessageListener {
    @Autowired
    NotifyBiz notifyBiz;

    /**
     * 消费商户回调通知的消息
     * @param message
     */
    @JmsListener(destination = TopicDest.MERCHANT_NOTIFY, concurrency = "1-50")
    private void merchantNotifyConsume(String message){
        CallbackDto callbackDto = JsonUtil.toBean(message, CallbackDto.class);
        notifyBiz.doNotifyAsync(callbackDto);
    }
}
