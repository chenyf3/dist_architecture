package com.xpay.service.message.biz.tts;

import com.xpay.facade.message.dto.TTSDto;

/**
 * 文本转语音接口(Text To Speech)
 */
public interface TTSTransfer {

    public byte[] transferAudio(TTSDto ttsDto);
}
