package com.xpay.service.message.serviceImpl;

import com.xpay.facade.message.dto.TTSDto;
import com.xpay.facade.message.service.TTSFacade;
import com.xpay.service.message.biz.tts.TTSBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class TTSFacadeImpl implements TTSFacade {
    @Autowired
    TTSBiz ttLBiz;

    @Override
    public byte[] onlineAudioTransfer(TTSDto ttsDto) {
        return ttLBiz.onlineAudioTransfer(ttsDto);
    }
}
