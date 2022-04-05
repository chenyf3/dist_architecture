package com.xpay.facade.message.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.message.dto.TTSDto;

public interface TTSFacade {

    /**
     * 在线语音转换
     * @param ttsDto
     * @return
     */
    public byte[] onlineAudioTransfer(TTSDto ttsDto) throws BizException;
}
