package com.xpay.service.message.biz.pusher;

import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.message.dto.PushDto;
import com.xpay.facade.message.enums.PushPlatformEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PusherBiz {
    @Autowired
    private AliCloudPusher aliCloudPusher;
    @Autowired
    private TencentPusher tencentPusher;

    public boolean pushMessage(PushDto pushDto) {
        PushPlatformEnum platform = PushPlatformEnum.getEnum(pushDto.getPlatform());
        MobilePusher mobilePusher = getMobilePusher(platform);
        return mobilePusher.pushMessage(pushDto);
    }

    private MobilePusher getMobilePusher(PushPlatformEnum platform){
        if (PushPlatformEnum.ALI_CLOUD == platform) {
            return aliCloudPusher;
        } else if (PushPlatformEnum.TENCENT_XG == platform) {
            return tencentPusher;
        } else {
            throw new BizException("未识别的推送平台！");
        }
    }
}
